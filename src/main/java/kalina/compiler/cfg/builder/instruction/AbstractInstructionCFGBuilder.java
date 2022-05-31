package kalina.compiler.cfg.builder.instruction;

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
import kalina.compiler.ast.expression.array.ASTArrayLHS;
import kalina.compiler.cfg.builder.Assert;
import kalina.compiler.cfg.builder.ExpressionValidator;
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.AssignArrayVariableInfo;
import kalina.compiler.cfg.data.GetVariableOrField;
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
import kalina.compiler.instructions.v2.assign.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.assign.ArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.assign.AssignInstruction;
import kalina.compiler.instructions.v2.ClassPropertyCallChainInstruction;
import kalina.compiler.instructions.v2.assign.FieldArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.assign.FieldAssignInstruction;
import kalina.compiler.instructions.v2.FunCallInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public abstract class AbstractInstructionCFGBuilder {
    private final AbstractExpressionConverter expressionConverter;
    private final TypeChecker typeChecker;
    private final Type returnType;
    private final OxmaFunctionInfoProvider functionInfoProvider;
    private final Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider;

    public AbstractInstructionCFGBuilder(
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
        var getVariableOrField  = new GetVariableOrField(localVariableTable, fieldInfoProvider);
        if (abstractAssignInstruction instanceof ASTAssignInstruction assignInstruction) {
            instruction = constructAssignInstruction(assignInstruction, getVariableOrField, localVariableTable);
        } else if (abstractAssignInstruction instanceof ASTArrayAssignInstruction assignInstruction) {
            instruction = constructArrayAssignInstruction(assignInstruction, getVariableOrField, localVariableTable);
        } else {
            throw new IllegalArgumentException("Unexpected type " + abstractAssignInstruction);
        }
        return instruction;
    }

    private AbstractAssignInstruction constructAssignInstruction(
            ASTAssignInstruction assignInstruction,
            GetVariableOrField getVariableOrField,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        List<GetVariableOrField.VariableOrFieldInfo> variableOrFieldInfos = assignInstruction.getLHS().stream()
                .map(getVariableOrField::getVariableOrFieldInfoOrElseThrow)
                .toList();
        final AbstractAssignInstruction instruction;
        if (variableOrFieldInfos.stream().allMatch(x -> x.fieldInfo.isPresent())) {
            instruction = constructFieldAssignInstruction(
                    assignInstruction,
                    variableOrFieldInfos.stream()
                            .map(x -> x.fieldInfo.get())
                            .toList(),
                    localVariableTable);
        } else if (variableOrFieldInfos.stream().allMatch(x -> x.typeAndIndex.isPresent())) {
            instruction = constructVariableAssignInstruction(
                    assignInstruction,
                    localVariableTable
            );
        } else {
            throw new IllegalArgumentException("Variable and field assigns should be places");
        }

        ExpressionValidator.validateAssignExpression(instruction);
        return instruction;
    }

    private AbstractAssignInstruction constructVariableAssignInstruction(
            ASTAssignInstruction assignInstruction,
            AbstractLocalVariableTable localVariableTable)
    {
        List<VariableInfo> variableInfos = assignInstruction.getLHS().stream()
                .map(name -> {
                    TypeAndIndex variableInfo = localVariableTable.findVariableOrElseThrow(name);
                    return new VariableInfo(name, variableInfo.getIndex(), variableInfo.getType());
                })
                .toList();

        return new AssignInstruction(
                variableInfos,
                assignInstruction.getRHS().stream()
                        .map(expr -> expressionConverter.convert(expr, localVariableTable, functionInfoProvider, fieldInfoProvider))
                        .toList()
        );
    }

    private AbstractAssignInstruction constructFieldAssignInstruction(
            ASTAssignInstruction assignInstruction,
            List<OxmaFieldInfo> oxmaFieldInfos,
            AbstractLocalVariableTable localVariableTable)
    {
        List<VariableInfo> variableInfos = oxmaFieldInfos.stream()
                .map(fieldInfo -> {
                    if (fieldInfo.isFinal()) {
                        throw new IllegalArgumentException("Cannot assign to final field " + fieldInfo.fieldName());
                    }
                    validateStaticContext(fieldInfo.isStatic(), fieldInfo.fieldName());
                    return new VariableInfo(
                            fieldInfo.fieldName(),
                            fieldInfo.type(),
                            fieldInfo
                    );
                })
                .toList();

        return new FieldAssignInstruction(
                variableInfos,
                assignInstruction.getRHS().stream()
                        .map(expr -> expressionConverter.convert(expr, localVariableTable, functionInfoProvider, fieldInfoProvider))
                        .toList()
        );
    }

    private AbstractAssignInstruction constructArrayAssignInstruction(
            ASTArrayAssignInstruction assignInstruction,
            GetVariableOrField getVariableOrField,
            AbstractLocalVariableTable localVariableTable) throws IncompatibleTypesException
    {
        List<GetVariableOrField.VariableOrFieldInfo> variableOrFieldInfos = assignInstruction.getLHS().stream()
                .map(lhs -> getVariableOrField.getVariableOrFieldInfoOrElseThrow(lhs.name()))
                .toList();
        final AbstractAssignInstruction instruction;
        if (variableOrFieldInfos.stream().allMatch(x -> x.fieldInfo.isPresent())) {
            instruction = constructFieldArrayAssign(
                    assignInstruction,
                    variableOrFieldInfos.stream().map(x -> x.fieldInfo.get()).toList(),
                    localVariableTable);
        } else if (variableOrFieldInfos.stream().allMatch(x -> x.typeAndIndex.isPresent())) {
            instruction = constructVariableArrayAssign(
                    assignInstruction,
                    variableOrFieldInfos.stream().map(x -> x.typeAndIndex.get()).toList(),
                    localVariableTable);
        } else {
            throw new IllegalArgumentException("Variable and field assigns should be places");
        }
        ExpressionValidator.validateAssignExpression(instruction);
        return instruction;
    }

    private AbstractAssignInstruction constructVariableArrayAssign(
            ASTArrayAssignInstruction assignInstruction,
            List<TypeAndIndex> typeAndIndices,
            AbstractLocalVariableTable localVariableTable)
    {
        List<VariableInfo> variableInfos = IntStream.range(0, assignInstruction.getLHS().size()).mapToObj(i -> {
            ASTArrayLHS lhs = assignInstruction.getLHS().get(i);
            TypeAndIndex typeAndIndex = typeAndIndices.get(i);
            Assert.isArray(typeAndIndex.getType());
            AssignArrayVariableInfo arrayVariableInfo = getAssignArrayVariableInfo(
                    lhs,
                    typeAndIndex.getType(),
                    localVariableTable
            );
            return new VariableInfo(
                    lhs.name(),
                    typeAndIndex.getIndex(),
                    typeAndIndex.getType(),
                    arrayVariableInfo);
        }).toList();

        return new ArrayElementAssignInstruction(
                variableInfos,
                assignInstruction.getRHS().stream()
                        .map(expr -> expressionConverter.convert(expr, localVariableTable, functionInfoProvider, fieldInfoProvider))
                        .toList()
        );
    }

    private AbstractAssignInstruction constructFieldArrayAssign(
            ASTArrayAssignInstruction assignInstruction,
            List<OxmaFieldInfo> oxmaFieldInfos,
            AbstractLocalVariableTable localVariableTable)
    {
        List<VariableInfo> variableInfos = IntStream.range(0, assignInstruction.getLHS().size()).mapToObj(i -> {
            ASTArrayLHS lhs = assignInstruction.getLHS().get(i);
            OxmaFieldInfo fieldInfo = oxmaFieldInfos.get(i);
            if (fieldInfo.isFinal()) {
                throw new IllegalArgumentException("Cannot assign to final field " + lhs.name());
            }
            validateStaticContext(fieldInfo.isStatic(), fieldInfo.fieldName());
            Assert.isArray(fieldInfo.type());
            AssignArrayVariableInfo arrayVariableInfo = getAssignArrayVariableInfo(
                    lhs,
                    fieldInfo.type(),
                    localVariableTable
            );
            return new VariableInfo(fieldInfo.fieldName(), fieldInfo.type(), arrayVariableInfo, fieldInfo);
        }).toList();

        return new FieldArrayElementAssignInstruction(
                variableInfos,
                assignInstruction.getRHS().stream()
                        .map(expr -> expressionConverter.convert(expr, localVariableTable, functionInfoProvider, fieldInfoProvider))
                        .toList()
        );
    }

    private AssignArrayVariableInfo getAssignArrayVariableInfo(ASTArrayLHS lhs, Type type, AbstractLocalVariableTable localVariableTable) {
        return new AssignArrayVariableInfo(
                lhs.indices().stream()
                        .map(expr -> expressionConverter.convert(expr, localVariableTable, functionInfoProvider, fieldInfoProvider))
                        .toList(),
                CFGUtils.getArrayElementType(type),
                CFGUtils.lowArrayDimension(type, lhs.indices().size()));
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

    protected abstract void validateStaticContext(boolean isStatic, String memberName);
}
