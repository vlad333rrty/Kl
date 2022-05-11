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
import kalina.compiler.syntax.parser2.data.AbstractLocalVariableTable;
import kalina.compiler.syntax.parser2.data.AssignArrayVariableInfo;
import kalina.compiler.syntax.parser2.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser2.data.TypeAndIndex;
import kalina.compiler.syntax.parser2.data.VariableInfo;
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
    private final GetFunctionInfoProvider getFunctionInfoProvider;

    public ClassTraverser(
            ILocalVariableTableFactory localVariableTableFactory,
            TypeChecker typeChecker,
            ASTExpressionConverter astExpressionConverter,
            GetFunctionInfoProvider getFunctionInfoProvider)
    {
        this.localVariableTableFactory = localVariableTableFactory;
        this.typeChecker = typeChecker;
        this.astExpressionConverter = astExpressionConverter;
        this.getFunctionInfoProvider = getFunctionInfoProvider;
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
                AbstractBasicBlock bb = convertExpressionBasicBlock(
                        expression,
                        getFunctionInfoProvider.getFunctionTable(classNode.getClassName()).orElseThrow(),
                        localVariableTable,
                        node.getReturnType());
                funBasicBlock.addAtTheEnd(bb);
            }
            result.add(funBasicBlock);
        }

        return result;
    }

    public Optional<AbstractBasicBlock> traverseScope(
            ASTMethodEntryNode node,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException, IncompatibleTypesException
    {
        AbstractLocalVariableTable childTable = localVariableTableFactory.createChildLocalVariableTable(localVariableTable);
        List<AbstractBasicBlock> result = new ArrayList<>();
        for (ASTExpression expression : node.getExpressions()) {
            AbstractBasicBlock bb = convertExpressionBasicBlock(expression, functionTable, childTable, returnType);
            result.add(bb);
        }
        if (result.isEmpty()) {
            return Optional.empty();
        }
        AbstractBasicBlock bb = result.stream().findFirst().get();
        IntStream.range(1, result.size()).mapToObj(result::get).forEach(bb::addAtTheEnd);
        return Optional.of(bb);
    }

    public AbstractBasicBlock convertExpressionBasicBlock(
            ASTExpression expression,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException, IncompatibleTypesException
    {
        Instruction instruction = convertExpression(expression, functionTable, localVariableTable, returnType);
        return new BasicBlock(instruction);
    }

    public Instruction convertExpression(
            ASTExpression expression,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException, IncompatibleTypesException
    {
        final Instruction instruction;
        if (expression instanceof ASTInitInstruction initInstruction) {
            instruction = constructInitInstruction(initInstruction, functionTable, localVariableTable);
        } else if (expression instanceof ASTAbstractAssignInstruction assignInstruction) {
            instruction = constructAssignInstruction(assignInstruction, functionTable, localVariableTable);
        } else if (expression instanceof ASTIfInstruction ifInstruction) {
            instruction = constructIfInstruction(ifInstruction, functionTable, localVariableTable, returnType);
        } else if (expression instanceof ASTFunCallExpression funCallExpression) {
            instruction = constructFunCallInstruction(funCallExpression, functionTable, localVariableTable);
        } else if (expression instanceof ASTForInstruction forInstruction) {
            instruction = constructForInstruction(forInstruction, functionTable, localVariableTable, returnType);
        } else if (expression instanceof ASTDoInstruction doInstruction) {
            instruction = constructDoInstruction(doInstruction, functionTable, localVariableTable, returnType);
        } else if (expression instanceof ASTReturnInstruction returnInstruction) {
            instruction = constructFunEndBasicBlock(returnInstruction, functionTable, localVariableTable, returnType);
        } else {
            throw new UnsupportedOperationException();
        }
        return instruction;
    }

    private Instruction constructDoInstruction(
            ASTDoInstruction doInstruction,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException, IncompatibleTypesException
    {
        CondExpression condExpression = astExpressionConverter.convertCondExpression(doInstruction.condition(), localVariableTable, functionTable);
        Optional<AbstractBasicBlock> entry = traverseScope(doInstruction.entry(), functionTable, localVariableTable, returnType);
        return new DoInstruction(entry, condExpression);
    }

    private Instruction constructForInstruction(
            ASTForInstruction forInstruction,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException, IncompatibleTypesException
    {
        AbstractLocalVariableTable childTable =localVariableTableFactory.createChildLocalVariableTable(localVariableTable);
        Optional<Instruction> declarations = forInstruction.declarations().isPresent()
                ? Optional.of(convertExpression(forInstruction.declarations().get(), functionTable, childTable, returnType))
                : Optional.empty();
        Optional<CondExpression> condition = forInstruction.condition().isPresent()
                ? Optional.of(astExpressionConverter.convertCondExpression(forInstruction.condition().get(), childTable, functionTable))
                : Optional.empty();
        Optional<Instruction> action = forInstruction.action().isPresent()
                ? Optional.of(constructAssignInstruction((ASTAssignInstruction) forInstruction.action().get(), functionTable, childTable))
                : Optional.empty();

        Optional<AbstractBasicBlock> entry = traverseScope(forInstruction.entry(), functionTable, childTable, returnType);

        return new ForInstruction(declarations, condition, action, entry);
    }

    private Instruction constructFunCallInstruction(
            ASTFunCallExpression funCallExpression,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable)
    {
        Expression funCall = astExpressionConverter.convert(funCallExpression, localVariableTable, functionTable);
        return new SimpleInstruction(funCall);
    }

    private Instruction constructFunEndBasicBlock(
            ASTReturnInstruction returnInstruction,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType)
    {
        Optional<Expression> expression = returnInstruction .getReturnExpression()
                .map(astExpr -> astExpressionConverter.convert(astExpr, localVariableTable, functionTable));
        Optional<ReturnValueInfo> returnValueInfo = expression.map(expr -> new ReturnValueInfo(returnType, expr));
        return new FunEndInstruction(returnValueInfo);
    }

    private Instruction constructIfInstruction(
            ASTIfInstruction ifInstruction,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable,
            Type returnType) throws CFGConversionException, IncompatibleTypesException
    {
        CondExpression condExpression = astExpressionConverter
                .convertCondExpression(ifInstruction.condExpression(), localVariableTable, functionTable);
        Optional<AbstractBasicBlock> thenEntry = traverseScope(ifInstruction.thenBr(), functionTable, localVariableTable, returnType);
        Optional<AbstractBasicBlock> elseEntry = ifInstruction.elseBr().isPresent()
                ? traverseScope(ifInstruction.elseBr().get(), functionTable, localVariableTable, returnType)
                : Optional.empty();

        return new IfInstruction(condExpression, thenEntry, elseEntry);
    }

    private Instruction constructAssignInstruction(
            ASTAbstractAssignInstruction abstractAssignInstruction,
            OxmaFunctionInfoProvider functionTable,
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
        return instruction;
    }

    private AssignInstruction constructAssignInstruction(
            ASTAssignInstruction assignInstruction,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        List<VariableInfo> variableInfos = assignInstruction.getLHS().stream()
                .map(name -> {
                    Optional<TypeAndIndex> variableInfoO = localVariableTable.findVariable(name);
                    if (variableInfoO.isEmpty()) {
                        logger.error("No info found for variable {}", name);
                        return null;
                    }
                    TypeAndIndex variableInfo = variableInfoO.get();
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
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        List<VariableInfo> variableInfos = assignInstruction.getLHS().stream()
                .map(lhs -> {
                    Optional<TypeAndIndex> variableInfoO = localVariableTable.findVariable(lhs.name());
                    if (variableInfoO.isEmpty()) {
                        logger.error("No info found for variable {}", lhs.name());
                        throw new IllegalArgumentException();
                    }
                    TypeAndIndex variableInfo = variableInfoO.get();
                    Assert.isArray(variableInfo.getType());
                    AssignArrayVariableInfo arrayVariableInfo = new AssignArrayVariableInfo(
                            lhs.indices().stream().map(expr -> astExpressionConverter.convert(expr, localVariableTable, functionTable)).toList(),
                            CFGUtils.getArrayElementType(variableInfo.getType()),
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

    private InitInstruction constructInitInstruction(
            ASTInitInstruction initInstruction,
            OxmaFunctionInfoProvider functionTable,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        Type type = initInstruction.lhs().type();
        if (!Assert.assertIsValidDeclarationType(type, typeChecker)) {
            throw new CFGConversionException("Wrong declaration type: " + type.getClassName());
        }
        initInstruction.lhs()
                .variableNames()
                .forEach(name -> Assert.assertMultipleVariableDeclarations(name, localVariableTable));
        List<VariableNameAndIndex> variableNameAndIndices = new ArrayList<>();
        initInstruction.lhs().variableNames().forEach(name -> {
            int index = localVariableTable.addVariable(name, type);
            variableNameAndIndices.add(new VariableNameAndIndex(name, index));
        });
        LHS lhs = new LHS(variableNameAndIndices, type);
        List<Expression> rhs = new ArrayList<>();
        IntStream.range(0, initInstruction.rhs().size())
                .forEach(i -> {
                    Expression convert = astExpressionConverter.convert(
                            initInstruction.rhs().get(i),
                            localVariableTable,
                            functionTable);
                    rhs.add(convert);
                });
        InitInstruction instruction = new InitInstruction(lhs, rhs);
        ExpressionValidator.validateInitExpression(instruction);
        return instruction;
    }
}
