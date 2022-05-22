package kalina.compiler.cfg.traverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import kalina.compiler.ast.ASTMethodEntryNode;
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
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.AssignArrayVariableInfo;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.data.TypeAndIndex;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.data.VariableInfo;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class MethodEntryTraverser {
    private static final Logger logger = LogManager.getLogger(MethodEntryTraverser.class);

    private final AbstractExpressionConverter expressionConverter;
    private final TypeChecker typeChecker;
    private final Type returnType;
    private final OxmaFunctionInfoProvider functionInfoProvider;
    private final Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider;

    public MethodEntryTraverser(
            AbstractExpressionConverter expressionConverter,
            TypeChecker typeChecker,
            Type returnType,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        this.expressionConverter = expressionConverter;
        this.typeChecker = typeChecker;
        this.returnType = returnType;
        this.functionInfoProvider = functionInfoProvider;
        this.fieldInfoProvider = fieldInfoProvider;
    }

    public List<AbstractBasicBlock> traverse(List<ASTExpression> methodEntry, AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException {
        List<AbstractBasicBlock> result = new ArrayList<>();
        for (ASTExpression expression : methodEntry) {
            AbstractBasicBlock bb = convertExpressionBasicBlock(expression, localVariableTable);
            result.add(bb);
        }

        return result;
    }

    public Optional<AbstractBasicBlock> traverseScope(
            ASTMethodEntryNode node,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        AbstractLocalVariableTable childTable = localVariableTable.createChildTable();
        List<AbstractBasicBlock> result = new ArrayList<>();
        for (ASTExpression expression : node.getExpressions()) {
            AbstractBasicBlock bb = convertExpressionBasicBlock(expression, childTable);
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
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        Instruction instruction = convertExpression(expression, localVariableTable);
        return new BasicBlock(instruction);
    }

    public Instruction convertExpression(
            ASTExpression expression,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        final Instruction instruction;
        if (expression instanceof ASTInitInstruction initInstruction) {
            instruction = constructInitInstruction(initInstruction, localVariableTable);
        } else if (expression instanceof ASTAbstractAssignInstruction assignInstruction) {
            instruction = constructAssignInstruction(assignInstruction, localVariableTable);
        } else if (expression instanceof ASTIfInstruction ifInstruction) {
            instruction = constructIfInstruction(ifInstruction, localVariableTable);
        } else if (expression instanceof ASTFunCallExpression funCallExpression) {
            instruction = constructFunCallInstruction(funCallExpression, localVariableTable);
        } else if (expression instanceof ASTForInstruction forInstruction) {
            instruction = constructForInstruction(forInstruction, localVariableTable);
        } else if (expression instanceof ASTDoInstruction doInstruction) {
            instruction = constructDoInstruction(doInstruction, localVariableTable);
        } else if (expression instanceof ASTReturnInstruction returnInstruction) {
            instruction = constructFunEndBasicBlock(returnInstruction, localVariableTable);
        } else {
            throw new UnsupportedOperationException();
        }
        return instruction;
    }

    private Instruction constructDoInstruction(
            ASTDoInstruction doInstruction,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        CondExpression condExpression = expressionConverter.convertCondExpression(doInstruction.condition(), localVariableTable, functionInfoProvider, fieldInfoProvider);
        Optional<AbstractBasicBlock> entry = traverseScope(doInstruction.entry(), localVariableTable);
        return new DoInstruction(entry, condExpression);
    }

    private Instruction constructForInstruction(
            ASTForInstruction forInstruction,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        AbstractLocalVariableTable childTable = localVariableTable.createChildTable();
        Optional<Instruction> declarations = forInstruction.declarations().isPresent()
                ? Optional.of(convertExpression(forInstruction.declarations().get(), childTable))
                : Optional.empty();
        Optional<CondExpression> condition = forInstruction.condition().isPresent()
                ? Optional.of(expressionConverter.convertCondExpression(forInstruction.condition().get(), childTable, functionInfoProvider, fieldInfoProvider))
                : Optional.empty();
        Optional<Instruction> action = forInstruction.action().isPresent()
                ? Optional.of(constructAssignInstruction((ASTAssignInstruction) forInstruction.action().get(), childTable))
                : Optional.empty();

        Optional<AbstractBasicBlock> entry = traverseScope(forInstruction.entry(), childTable);

        return new ForInstruction(declarations, condition, action, entry);
    }

    private Instruction constructFunCallInstruction(
            ASTFunCallExpression funCallExpression,
            AbstractLocalVariableTable localVariableTable)
    {
        Expression funCall = expressionConverter.convert(funCallExpression, localVariableTable, functionInfoProvider, fieldInfoProvider);
        return new SimpleInstruction(funCall);
    }

    private Instruction constructFunEndBasicBlock(
            ASTReturnInstruction returnInstruction,
            AbstractLocalVariableTable localVariableTable)
    {
        Optional<Expression> expression = returnInstruction .getReturnExpression()
                .map(astExpr -> expressionConverter.convert(astExpr, localVariableTable, functionInfoProvider, fieldInfoProvider));
        Optional<ReturnValueInfo> returnValueInfo = expression.map(expr -> new ReturnValueInfo(returnType, expr));
        return new FunEndInstruction(returnValueInfo);
    }

    private Instruction constructIfInstruction(
            ASTIfInstruction ifInstruction,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException
    {
        CondExpression condExpression = expressionConverter
                .convertCondExpression(ifInstruction.condExpression(), localVariableTable, functionInfoProvider, fieldInfoProvider);
        Optional<AbstractBasicBlock> thenEntry = traverseScope(ifInstruction.thenBr(), localVariableTable);
        Optional<AbstractBasicBlock> elseEntry = ifInstruction.elseBr().isPresent()
                ? traverseScope(ifInstruction.elseBr().get(), localVariableTable)
                : Optional.empty();

        return new IfInstruction(condExpression, thenEntry, elseEntry);
    }

    private Instruction constructAssignInstruction(
            ASTAbstractAssignInstruction abstractAssignInstruction,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        final AbstractAssignInstruction instruction;
        if (abstractAssignInstruction instanceof ASTAssignInstruction assignInstruction) {
            instruction = constructAssignInstruction(assignInstruction, localVariableTable);
        } else if (abstractAssignInstruction instanceof ASTArrayAssignInstruction assignInstruction) {
            instruction = constructArrayAssignInstruction(assignInstruction, localVariableTable);
        } else {
            throw new IllegalArgumentException("Unexpected type " + abstractAssignInstruction);
        }
        return instruction;
    }

    private AssignInstruction constructAssignInstruction(
            ASTAssignInstruction assignInstruction,
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
                        .map(expr -> expressionConverter.convert(expr, localVariableTable, functionInfoProvider, fieldInfoProvider))
                        .toList()
        );
        ExpressionValidator.validateAssignExpression(instruction);
        return instruction;
    }

    private ArrayAssignInstruction constructArrayAssignInstruction(
            ASTArrayAssignInstruction assignInstruction,
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
                            lhs.indices().stream().map(expr -> expressionConverter.convert(expr, localVariableTable, functionInfoProvider, fieldInfoProvider)).toList(),
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
                        .map(expr -> expressionConverter.convert(expr, localVariableTable, functionInfoProvider, fieldInfoProvider))
                        .toList()
        );
        ExpressionValidator.validateAssignExpression(instruction);
        return instruction;
    }

    private InitInstruction constructInitInstruction(
            ASTInitInstruction initInstruction,
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
                    Expression convert = expressionConverter.convert(
                            initInstruction.rhs().get(i),
                            localVariableTable,
                            functionInfoProvider,
                            fieldInfoProvider);
                    rhs.add(convert);
                });
        InitInstruction instruction = new InitInstruction(lhs, rhs);
        ExpressionValidator.validateInitExpression(instruction);
        return instruction;
    }
}
