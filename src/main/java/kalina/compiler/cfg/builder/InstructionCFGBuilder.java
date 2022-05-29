package kalina.compiler.cfg.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import kalina.compiler.ast.expression.ASTAbstractAssignInstruction;
import kalina.compiler.ast.expression.ASTAssignInstruction;
import kalina.compiler.ast.expression.ASTClassPropertyCallExpression;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTFunCallExpression;
import kalina.compiler.ast.expression.ASTInitInstruction;
import kalina.compiler.ast.expression.ASTReturnInstruction;
import kalina.compiler.ast.expression.array.ASTArrayAssignInstruction;
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.AssignArrayVariableInfo;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.data.TypeAndIndex;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.ReturnValueInfo;
import kalina.compiler.expressions.VariableNameAndIndex;
import kalina.compiler.expressions.v2.ClassPropertyCallExpression;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import kalina.compiler.instructions.FunEndInstruction;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.ArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.AssignInstruction;
import kalina.compiler.instructions.v2.ClassPropertyCallChainInstruction;
import kalina.compiler.instructions.v2.FunCallInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class InstructionCFGBuilder {
    private static final Logger logger = LogManager.getLogger(MethodEntryCFGBuilder.class);

    private final AbstractExpressionConverter expressionConverter;
    private final TypeChecker typeChecker;
    private final Type returnType;
    private final OxmaFunctionInfoProvider functionInfoProvider;
    private final Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider;

    public InstructionCFGBuilder(
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

    public Instruction constructInstruction(
            ASTExpression expression,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        final Instruction instruction;
        if (expression instanceof ASTInitInstruction initInstruction) {
            instruction = constructInitInstruction(initInstruction, localVariableTable);
        } else if (expression instanceof ASTAbstractAssignInstruction assignInstruction) {
            instruction = constructAssignInstruction(assignInstruction, localVariableTable);
        } else if (expression instanceof ASTFunCallExpression funCallExpression) {
            instruction = constructFunCallInstruction(funCallExpression, localVariableTable);
        } else if (expression instanceof ASTReturnInstruction returnInstruction) {
            instruction = constructFunEndBasicBlock(returnInstruction, localVariableTable);
        } else if (expression instanceof ASTClassPropertyCallExpression classPropertyCallExpression) {
            instruction = convertClassPropertyCallChainInstruction(classPropertyCallExpression, localVariableTable);
        } else {
            throw new UnsupportedOperationException(expression.toString());
        }
        return instruction;
    }

    private ClassPropertyCallChainInstruction convertClassPropertyCallChainInstruction(
            ASTClassPropertyCallExpression classPropertyCallExpression,
            AbstractLocalVariableTable localVariableTable)
    {
        ClassPropertyCallExpression expression = (ClassPropertyCallExpression)expressionConverter
                .convert(classPropertyCallExpression, localVariableTable, functionInfoProvider, fieldInfoProvider);
        return new ClassPropertyCallChainInstruction(expression);
    }

    private Instruction constructFunCallInstruction(
            ASTFunCallExpression funCallExpression,
            AbstractLocalVariableTable localVariableTable)
    {
        AbstractFunCallExpression funCall = (AbstractFunCallExpression)expressionConverter.convert(funCallExpression, localVariableTable, functionInfoProvider, fieldInfoProvider);
        return new FunCallInstruction(funCall);
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
                        throw new IllegalArgumentException("Undeclared variable " + name);
                    }
                    TypeAndIndex variableInfo = variableInfoO.get();
                    return new VariableInfo(name, variableInfo.getIndex(), variableInfo.getType());
                })
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

    private ArrayElementAssignInstruction constructArrayAssignInstruction(
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

        ArrayElementAssignInstruction instruction = new ArrayElementAssignInstruction(
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
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        Type type = initInstruction.lhs().type();
        Assert.assertIsValidDeclarationType(type, typeChecker);
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
