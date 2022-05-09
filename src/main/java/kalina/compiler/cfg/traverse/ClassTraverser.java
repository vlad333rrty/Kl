package kalina.compiler.cfg.traverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTMethodEntryNode;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.ast.expression.ASTAbstractAssignInstruction;
import kalina.compiler.ast.expression.ASTAssignInstruction;
import kalina.compiler.ast.expression.ASTDoInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.ast.expression.ASTFunCallExpression;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.ast.expression.ASTInitInstruction;
import kalina.compiler.ast.expression.ASTReturnInstruction;
import kalina.compiler.ast.expression.array.ASTArrayAssignInstruction;
import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.BasicBlock;
import kalina.compiler.bb.FunBasicBlock;
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.converter.ASTExpressionConverter;
import kalina.compiler.cfg.converter.ASTInitExpressionConverter;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.ReturnValueInfo;
import kalina.compiler.expressions.VariableNameAndIndex;
import kalina.compiler.instructions.DoInstruction;
import kalina.compiler.instructions.FunEndInstruction;
import kalina.compiler.instructions.IfInstruction;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.SimpleInstruction;
import kalina.compiler.instructions.v2.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.ArrayAssignInstruction;
import kalina.compiler.instructions.v2.AssignInstruction;
import kalina.compiler.instructions.v2.ForInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.syntax.parser.data.AbstractLocalVariableTable;
import kalina.compiler.syntax.parser.data.AssignArrayVariableInfo;
import kalina.compiler.syntax.parser.data.ExtendedVariableInfo;
import kalina.compiler.syntax.parser.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.VariableInfo;
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
    private final ASTInitExpressionConverter initExpressionConverter;

    public ClassTraverser(
            ILocalVariableTableFactory localVariableTableFactory,
            TypeChecker typeChecker,
            ASTExpressionConverter astExpressionConverter,
            ASTInitExpressionConverter initExpressionConverter)
    {
        this.localVariableTableFactory = localVariableTableFactory;
        this.typeChecker = typeChecker;
        this.astExpressionConverter = astExpressionConverter;
        this.initExpressionConverter = initExpressionConverter;
    }

    public List<AbstractBasicBlock> traverse(ASTClassNode classNode) throws CFGConversionException, IncompatibleTypesException {
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
            Type returnType) throws CFGConversionException, IncompatibleTypesException
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
            Type returnType) throws CFGConversionException, IncompatibleTypesException
    {
        final AbstractBasicBlock bb;
        if (expression instanceof ASTInitInstruction initInstruction) {
            bb = constructInitBasicBlock(initInstruction, functionTable, localVariableTable);
        } else if (expression instanceof ASTAbstractAssignInstruction assignInstruction) {
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
            Type returnType) throws CFGConversionException, IncompatibleTypesException
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
            Type returnType) throws CFGConversionException, IncompatibleTypesException
    {
        Optional<InitInstruction> declarations = forInstruction.declarations().isPresent()
                ? Optional.of(constructInitInstruction(forInstruction.declarations().get(), functionTable, localVariableTable))
                : Optional.empty();
        Optional<CondExpression> condition = forInstruction.condition().isPresent()
                ? Optional.of(astExpressionConverter.convertCondExpression(forInstruction.condition().get(), localVariableTable, functionTable))
                : Optional.empty();
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
            AbstractLocalVariableTable localVariableTable)
    {
        Expression funCall = astExpressionConverter.convert(funCallExpression, localVariableTable, functionTable);
        return new BasicBlock(new SimpleInstruction(funCall));
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
            Type returnType) throws CFGConversionException, IncompatibleTypesException
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
            ASTAbstractAssignInstruction abstractAssignInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        final AbstractAssignInstruction instruction;
        if (abstractAssignInstruction instanceof ASTAssignInstruction assignInstruction) {
            instruction = constructAssignInstruction(assignInstruction, functionTable, localVariableTable);
        } else if (abstractAssignInstruction instanceof ASTArrayAssignInstruction assignInstruction) {
            instruction = constructArrayAssignInstruction(assignInstruction, functionTable, localVariableTable);
        } else {
            throw new IllegalArgumentException("Unexpected type " + abstractAssignInstruction);
        }
        return new BasicBlock(instruction);
    }

    private AssignInstruction constructAssignInstruction(
            ASTAssignInstruction assignInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        List<VariableInfo> variableInfos = assignInstruction.getLHS().stream()
                .map(name -> {
                    Optional<ExtendedVariableInfo> variableInfoO = localVariableTable.findVariable(name);
                    if (variableInfoO.isEmpty()) {
                        logger.error("No info found for variable {}", name);
                        return null;
                    }
                    ExtendedVariableInfo variableInfo = variableInfoO.get();
                    return new VariableInfo(name, variableInfo.getIndex(), variableInfo.getType());
                })
                .filter(Objects::nonNull)
                .toList();

        AssignInstruction instruction = new AssignInstruction(
                variableInfos,
                assignInstruction.getRHS().stream()
                        .map(expr -> astExpressionConverter.convert(expr, localVariableTable, functionTable))
                        .toList()
        );
        ExpressionValidator.validateAssignExpression(instruction);
        return instruction;
    }

    private ArrayAssignInstruction constructArrayAssignInstruction(
            ASTArrayAssignInstruction assignInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        List<VariableInfo> variableInfos = assignInstruction.getLHS().stream()
                .map(lhs -> {
                    Optional<ExtendedVariableInfo> variableInfoO = localVariableTable.findVariable(lhs.name());
                    if (variableInfoO.isEmpty()) {
                        logger.error("No info found for variable {}", lhs.name());
                        throw new IllegalArgumentException();
                    }
                    ExtendedVariableInfo variableInfo = variableInfoO.get();
                    Assert.isArray(variableInfo.getType());
                    if (variableInfo.getArrayVariableInfo().isEmpty()) {
                        logger.error("No array info found for variable {}", lhs.name());
                        throw new IllegalArgumentException();
                    }
                    AssignArrayVariableInfo arrayVariableInfo = new AssignArrayVariableInfo(
                            lhs.indices(),
                            variableInfo.getArrayVariableInfo().get().getElementType(),
                            CFGUtils.lowArrayDimension(variableInfo.getType(), lhs.indices().size()));
                    return new VariableInfo(
                            lhs.name(),
                            variableInfo.getIndex(),
                            variableInfo.getType(),
                            arrayVariableInfo);
                })
                .toList();

        ArrayAssignInstruction instruction = new ArrayAssignInstruction(
                variableInfos,
                assignInstruction.getRHS().stream()
                        .map(expr -> astExpressionConverter.convert(expr, localVariableTable, functionTable))
                        .toList()
        );
        ExpressionValidator.validateAssignExpression(instruction);
        return instruction;
    }

    private AbstractBasicBlock constructInitBasicBlock(
            ASTInitInstruction initInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        InitInstruction instruction = constructInitInstruction(initInstruction, functionTable, localVariableTable);
        return new BasicBlock(instruction);
    }

    private InitInstruction constructInitInstruction(
            ASTInitInstruction initInstruction,
            OxmaFunctionTable functionTable,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        Type type = initInstruction.lhs().type();
        if (!Assert.assertIsValidDeclarationType(type, typeChecker)) {
            throw new CFGConversionException("Wrong declaration type");
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
        List<Expression> rhs = new ArrayList<>();
        IntStream.range(0, initInstruction.rhs().size())
                .forEach(i -> {
                    Expression convert = initExpressionConverter.convert(
                            initInstruction.rhs().get(i),
                            localVariableTable,
                            functionTable,
                            lhs.getVars().get(i).getName());
                    rhs.add(convert);
                });
        InitInstruction instruction = new InitInstruction(lhs, rhs);
        ExpressionValidator.validateInitExpression(instruction);
        return instruction;
    }
}
