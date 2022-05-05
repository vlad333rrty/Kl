package kalina.compiler.cfg.traverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTMethodEntryNode;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.ast.expression.ASTAssignInstruction;
import kalina.compiler.ast.expression.ASTDoInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.ast.expression.ASTFunCallExpression;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.ast.expression.ASTInitInstruction;
import kalina.compiler.ast.expression.ASTReturnInstruction;
import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.BasicBlock;
import kalina.compiler.bb.FunBasicBlock;
import kalina.compiler.cfg.converter.ASTExpressionConverter;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.ReturnValueInfo;
import kalina.compiler.expressions.VariableNameAndIndex;
import kalina.compiler.expressions.v2.FunCallExpression;
import kalina.compiler.instructions.DoInstruction;
import kalina.compiler.instructions.v2.ForInstruction;
import kalina.compiler.instructions.FunEndInstruction;
import kalina.compiler.instructions.IfInstruction;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.SimpleInstruction;
import kalina.compiler.instructions.v2.AssignInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.odk.ClassAndSignature;
import kalina.compiler.odk.ODKMapper;
import kalina.compiler.syntax.parser.data.AbstractLocalVariableTable;
import kalina.compiler.syntax.parser.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.TypeAndIndex;
import kalina.compiler.syntax.parser.data.VariableInfo;
import kalina.compiler.syntax.parser2.data.OxmaFunctionInfo;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ClassTraverser {
    private static final Logger logger = LogManager.getLogger(ClassTraverser.class);

    private final ILocalVariableTableFactory localVariableTableFactory;
    private final TypeChecker typeChecker;
    private final ASTExpressionConverter astExpressionConverter;

    public ClassTraverser(
            ILocalVariableTableFactory localVariableTableFactory,
            TypeChecker typeChecker,
            ASTExpressionConverter astExpressionConverter)
    {
        this.localVariableTableFactory = localVariableTableFactory;
        this.typeChecker = typeChecker;
        this.astExpressionConverter = astExpressionConverter;
    }

    public List<AbstractBasicBlock> traverse(ASTClassNode classNode) throws CFGConversionException {
        List<AbstractBasicBlock> result = new ArrayList<>();
        for (ASTMethodNode node : classNode.getMethodNodes()) {
            FunBasicBlock funBasicBlock =
                    new FunBasicBlock(node.getName(), node.getArgs(), Optional.of(node.getReturnType()), node.isStatic());
            AbstractLocalVariableTable localVariableTable = node.isStatic()
                    ? localVariableTableFactory.createLocalVariableTableForStatic()
                    : localVariableTableFactory.createLocalVariableTableForNonStatic();
            node.getArgs().forEach(arg -> localVariableTable.addVariable(arg.getName(), arg.getType()));
            for (ASTExpression expression : node.getExpressions()) {
                AbstractBasicBlock bb = convertExpression(expression, classNode.getOxmaFunctionTable(), localVariableTable, node.getReturnType());
                funBasicBlock.addAtTheEnd(bb);
            }
            result.add(funBasicBlock);
        }

        return result;
    }

    public Optional<AbstractBasicBlock> traverseScope(
            ASTMethodEntryNode node,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException
    {
        AbstractLocalVariableTable childTable = localVariableTableFactory.createChildLocalVariableTable(localVariableTable);
        List<AbstractBasicBlock> result = new ArrayList<>();
        for (ASTExpression expression : node.getExpressions()) {
            AbstractBasicBlock bb = convertExpression(expression, functionTable, childTable, returnType);
            result.add(bb);
        }
        if (result.isEmpty()) {
            return Optional.empty();
        }
        AbstractBasicBlock bb = result.stream().findFirst().get();
        IntStream.range(1, result.size()).mapToObj(result::get).forEach(bb::addAtTheEnd);
        return Optional.of(bb);
    }

    public AbstractBasicBlock convertExpression(
            ASTExpression expression,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException
    {
        final AbstractBasicBlock bb;
        if (expression instanceof ASTInitInstruction initInstruction) {
            bb = constructInitBasicBlock(initInstruction, functionTable, localVariableTable);
        } else if (expression instanceof ASTAssignInstruction assignInstruction) {
            bb = constructAssignBasicBlock(assignInstruction, functionTable, localVariableTable);
        } else if (expression instanceof ASTIfInstruction ifInstruction) {
            bb = constructIfBasicBlock(ifInstruction, functionTable, localVariableTable, returnType);
        } else if (expression instanceof ASTFunCallExpression funCallExpression) {
            bb = constructFunCallBasicBlock(funCallExpression, functionTable, localVariableTable);
        } else if (expression instanceof ASTForInstruction forInstruction) {
            bb = constructForBasicBlock(forInstruction, functionTable, localVariableTable, returnType);
        } else if (expression instanceof ASTDoInstruction doInstruction) {
            bb = constructDoInstruction(doInstruction, functionTable, localVariableTable, returnType);
        } else if (expression instanceof ASTReturnInstruction returnInstruction) {
            bb = constructFunEndBasicBlock(returnInstruction, functionTable, localVariableTable, returnType);
        } else {
            throw new UnsupportedOperationException();
        }
        return bb;
    }

    private AbstractBasicBlock constructDoInstruction(
            ASTDoInstruction doInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException
    {
        CondExpression condExpression = astExpressionConverter.convertCondExpression(doInstruction.condition(), localVariableTable, functionTable);
        Optional<AbstractBasicBlock> entry = traverseScope(doInstruction.entry(), functionTable, localVariableTable, returnType);
        DoInstruction instruction = new DoInstruction(entry, condExpression);
        return new BasicBlock(instruction);
    }

    private AbstractBasicBlock constructForBasicBlock(
            ASTForInstruction forInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException
    {
        Optional<InitInstruction> declarations = forInstruction.declarations().isPresent()
                ? Optional.of(constructInitInstruction(forInstruction.declarations().get(), functionTable, localVariableTable))
                : Optional.empty();
        Optional<CondExpression> condition = forInstruction.condition()
                .map(cond -> astExpressionConverter.convertCondExpression(cond, localVariableTable, functionTable));
        Optional<Instruction> action = forInstruction.action().isPresent()
                ? Optional.of(constructAssignInstruction((ASTAssignInstruction) forInstruction.action().get(), functionTable, localVariableTable))
                : Optional.empty();

        Optional<AbstractBasicBlock> entry = traverseScope(forInstruction.entry(), functionTable, localVariableTable, returnType);

        ForInstruction instruction = new ForInstruction(declarations, condition, action, entry);
        return new BasicBlock(instruction);
    }

    private AbstractBasicBlock constructFunCallBasicBlock(
            ASTFunCallExpression funCallExpression,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException
    {
        List<Expression> funArgs = funCallExpression.arguments().stream()
                .map(arg -> astExpressionConverter.convert(arg, localVariableTable, functionTable))
                .toList();
        List<Type> signature = funArgs.stream().map(Expression::getType).toList();
        String funName = funCallExpression.funName();
        Optional<OxmaFunctionInfo> functionInfo = functionTable.getFunctionInfo(funName, signature);

        final Instruction instruction;
        if (functionInfo.isEmpty()) {
            Optional<ClassAndSignature> stdFun = ODKMapper.getO(funName);
            if (stdFun.isEmpty()) {
                logger.error("");
                throw new CFGConversionException();
            }
            try {
                instruction = stdFun.get().createInstruction(funArgs);
            } catch (Exception e) {
                throw new CFGConversionException();
            }
        } else {
            FunCallExpression funCall = new FunCallExpression(funName, funArgs, functionInfo.get());
            instruction = new SimpleInstruction(funCall);
        }

        return new BasicBlock(instruction);
    }

    private AbstractBasicBlock constructFunEndBasicBlock(
            ASTReturnInstruction returnInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType)
    {
        Optional<Expression> expression = returnInstruction .getReturnExpression()
                .map(astExpr -> astExpressionConverter.convert(astExpr, localVariableTable, functionTable));
        Optional<ReturnValueInfo> returnValueInfo = expression.map(expr -> new ReturnValueInfo(returnType, expr));
        return new BasicBlock(new FunEndInstruction(returnValueInfo));
    }

    private AbstractBasicBlock constructIfBasicBlock(
            ASTIfInstruction ifInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException
    {
        CondExpression condExpression = astExpressionConverter
                .convertCondExpression(ifInstruction.condExpression(), localVariableTable, functionTable);
        Optional<AbstractBasicBlock> thenEntry = traverseScope(ifInstruction.thenBr(), functionTable, localVariableTable, returnType);
        Optional<AbstractBasicBlock> elseEntry = ifInstruction.elseBr().isPresent()
                ? traverseScope(ifInstruction.elseBr().get(), functionTable, localVariableTable, returnType)
                : Optional.empty();

        IfInstruction instruction = new IfInstruction(condExpression, thenEntry, elseEntry);
        return new BasicBlock(instruction);
    }

    private AbstractBasicBlock constructAssignBasicBlock(
            ASTAssignInstruction assignInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable)
    {
        AssignInstruction instruction = constructAssignInstruction(assignInstruction, functionTable, localVariableTable);
        return new BasicBlock(instruction);
    }

    private AssignInstruction constructAssignInstruction(
            ASTAssignInstruction assignInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable)
    {
        List<VariableInfo> variableInfos = assignInstruction.lhs().stream()
                .map(name -> {
                    Optional<TypeAndIndex> typeAndIndexO = localVariableTable.findVariable(name);
                    if (typeAndIndexO.isEmpty()) {
                        logger.error("No info found for variable {}", name);
                        return null;
                    }
                    return new VariableInfo(name, typeAndIndexO.get().getIndex(), typeAndIndexO.get().getType());
                })
                .filter(Objects::nonNull)
                .toList();

        return new AssignInstruction(
                variableInfos,
                assignInstruction.rhs().stream()
                        .map(expr -> astExpressionConverter.convert(expr, localVariableTable, functionTable))
                        .toList()
        );
    }

    private AbstractBasicBlock constructInitBasicBlock(
            ASTInitInstruction initInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException
    {
        InitInstruction instruction = constructInitInstruction(initInstruction, functionTable, localVariableTable);
        return new BasicBlock(instruction);
    }

    private InitInstruction constructInitInstruction(
            ASTInitInstruction initInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException
    {
        Type type = initInstruction.lhs().type();
        if (!Assert.assertIsValidDeclarationType(type, typeChecker)) {
            throw new CFGConversionException();
        }
        List<String> filteredVariableNames = initInstruction.lhs()
                .variableNames()
                .stream()
                .filter(name -> Assert.assertMultipleVariableDeclarations(name, localVariableTable))
                .toList();
        List<VariableNameAndIndex> variableNameAndIndices = new ArrayList<>();
        filteredVariableNames.forEach(name -> {
            int index = localVariableTable.addVariable(name, type);
            variableNameAndIndices.add(new VariableNameAndIndex(name, index));
        });
        LHS lhs = new LHS(variableNameAndIndices, type);
        List<Expression> rhs = initInstruction.rhs().stream()
                .map(expr -> astExpressionConverter.convert(expr, localVariableTable, functionTable))
                .toList();
        return new InitInstruction(lhs, rhs);
    }
}
