package kalina.compiler.cfg.converter;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;

import kalina.compiler.ast.expression.ASTArithmeticExpression;
import kalina.compiler.ast.expression.ASTClassPropertyCallExpression;
import kalina.compiler.ast.expression.ASTCondExpression;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTFactor;
import kalina.compiler.ast.expression.ASTFunCallExpression;
import kalina.compiler.ast.expression.ASTObjectCreationExpression;
import kalina.compiler.ast.expression.ASTTerm;
import kalina.compiler.ast.expression.ASTThisExpression;
import kalina.compiler.ast.expression.ASTValueExpression;
import kalina.compiler.ast.expression.ASTVariableExpression;
import kalina.compiler.ast.expression.array.ASTArrayCreationExpression;
import kalina.compiler.ast.expression.array.ASTArrayGetElementExpression;
import kalina.compiler.ast.expression.field.ASTFieldAccessExpression;
import kalina.compiler.ast.expression.field.ASTOtherFieldAccessExpression;
import kalina.compiler.ast.expression.field.ASTUnknownOwnerFieldExpression;
import kalina.compiler.ast.expression.method.ASTMethodCallExpression;
import kalina.compiler.ast.expression.method.ASTUnknownOwnerMethodCall;
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.converter.items.ArrayGetElementExpressionConverter;
import kalina.compiler.cfg.converter.items.MethodCallExpressionConverter;
import kalina.compiler.cfg.converter.items.OtherFieldExpressionsConverter;
import kalina.compiler.cfg.converter.items.UnknownOwnerFieldExpressionConverter;
import kalina.compiler.cfg.converter.items.UnknownOwnerMethodCallExpressionConverter;
import kalina.compiler.cfg.converter.items.VariableOrFieldExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;
import kalina.compiler.cfg.data.GetVariableOrField;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.OxmaFunctionInfo;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.cfg.validator.Validator;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.ObjectCreationExpression;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.v2.ClassPropertyCallExpression;
import kalina.compiler.expressions.v2.ThisExpression;
import kalina.compiler.expressions.v2.array.ArrayWithCapacityCreationExpression;
import kalina.compiler.expressions.v2.field.FieldAccessExpression;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import kalina.compiler.expressions.v2.funCall.FunCallExpression;
import kalina.compiler.odk.ODKMapper;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public abstract class AbstractExpressionConverter {
    private static final Logger logger = LogManager.getLogger(ExpressionConverter.class);

    private final GetFunctionInfoProvider getFunctionInfoProvider;
    private final GetFieldInfoProvider getFieldInfoProvider;
    private final Stack<Expression> expressionStack = new Stack<>();

    public AbstractExpressionConverter(
            GetFunctionInfoProvider getFunctionInfoProvider,
            GetFieldInfoProvider getFieldInfoProvider)
    {
        this.getFunctionInfoProvider = getFunctionInfoProvider;
        this.getFieldInfoProvider = getFieldInfoProvider;
    }

    public Expression convert(
            ASTExpression astExpression,
            AbstractLocalVariableTable localVariableTable,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        expressionStack.clear();
        return convert(
                astExpression,
                new GetVariableOrField(localVariableTable, fieldInfoProvider),
                functionInfoProvider,
                fieldInfoProvider
        );
    }

    private Expression convert(
            ASTExpression astExpression,
            GetVariableOrField getVariableOrField,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        if (astExpression instanceof ASTValueExpression valueExpression) {
            return new ValueExpression(valueExpression.value(), valueExpression.type());
        }
        if (astExpression instanceof ASTVariableExpression variableExpression) {
            return expressionStack.push(VariableOrFieldExpressionConverter.convert(getVariableOrField, variableExpression));
        }
        if (astExpression instanceof ASTFactor factor) {
            Expression expression = convert(factor.expression(), getVariableOrField, functionInfoProvider, fieldInfoProvider);
            return factor.shouldNegate() ? Factor.createNegateFactor(expression) : Factor.createFactor(expression);
        }
        if (astExpression instanceof ASTTerm term) {
            List<Factor> factors = term.factors().stream()
                    .map(factor -> (Factor)convert(factor, getVariableOrField, functionInfoProvider, fieldInfoProvider)).toList();
            return new Term(factors, term.operations());
        }
        if (astExpression instanceof ASTArithmeticExpression arithmeticExpression) {
            List<Term> terms = arithmeticExpression.terms().stream()
                    .map(term -> (Term)convert(term, getVariableOrField, functionInfoProvider, fieldInfoProvider)).toList();
            return new ArithmeticExpression(terms, arithmeticExpression.operations());
        }
        if (astExpression instanceof ASTFunCallExpression funCallExpression) {
            List<Expression> arguments = funCallExpression.arguments().stream()
                    .map(arg -> convert(arg, getVariableOrField, functionInfoProvider, fieldInfoProvider))
                    .toList();
            String funName = funCallExpression.funName();
            Optional<OxmaFunctionInfo> functionInfoO = functionInfoProvider
                    .getFunctionInfo(funName, CFGUtils.getSignatureFromExpressions(arguments));
            if (functionInfoO.isEmpty()) {
                return tryToFindStdFun(funName, arguments);
            }
            OxmaFunctionInfo functionInfo = functionInfoO.get();
            validateStaticContext(functionInfo, funName);
            var expr = new FunCallExpression(funName, arguments, functionInfo, functionInfo.isStatic() ? Optional.empty() : Optional.of(new ThisExpression()));
            if (!expr.getType().equals(Type.VOID_TYPE)) {
                expressionStack.push(expr);
            }
            return expr;
        }
        if (astExpression instanceof ASTObjectCreationExpression objectCreationExpression) {
            List<Expression> arguments = objectCreationExpression.arguments().stream()
                    .map(arg -> convert(arg, getVariableOrField, functionInfoProvider, fieldInfoProvider))
                    .toList();
            return expressionStack.push(new ObjectCreationExpression(objectCreationExpression.className(), arguments));
        }
        if (astExpression instanceof ASTMethodCallExpression methodCallExpression) {
            Expression methodExpr = MethodCallExpressionConverter.convert(
                    methodCallExpression,
                    getVariableOrField,
                    getFunctionInfoProvider,
                    (expr, funInfoProvider) -> convert(expr, getVariableOrField, funInfoProvider, fieldInfoProvider)
            );
            if (!methodExpr.getType().equals(Type.VOID_TYPE)) {
                expressionStack.push(methodExpr);
            }
            return methodExpr;
        }
        if (astExpression instanceof ASTArrayCreationExpression arrayCreationExpression) {
            List<Expression> capacities = arrayCreationExpression.getCapacities().stream()
                    .map(e -> convert(e, getVariableOrField, functionInfoProvider, fieldInfoProvider))
                    .toList();
            for (Expression capacity : capacities) {
                if (capacity.getType() != Type.INT_TYPE) {
                    throw new IllegalArgumentException("Unexpected type used for array creation: " + capacity.getType().getClassName());
                }
            }
            return new ArrayWithCapacityCreationExpression(
                    arrayCreationExpression.getCapacities().stream().map(e -> convert(e, getVariableOrField, functionInfoProvider, fieldInfoProvider)).toList(),
                    arrayCreationExpression.getArrayType(),
                    arrayCreationExpression.getElementType());
        }
        if (astExpression instanceof ASTArrayGetElementExpression getElementExpression) {
            return expressionStack.push(ArrayGetElementExpressionConverter.convert(
                    getVariableOrField,
                    getElementExpression,
                    getElementExpression.getIndices().stream()
                            .map(expr -> convert(expr, getVariableOrField, functionInfoProvider, fieldInfoProvider))
                            .toList()
            ));
        }
        if (astExpression instanceof ASTOtherFieldAccessExpression otherFieldAccessExpression) {
            return expressionStack.push(OtherFieldExpressionsConverter.convert(otherFieldAccessExpression, getVariableOrField, getFieldInfoProvider));
        }
        if (astExpression instanceof ASTUnknownOwnerFieldExpression unknownOwnerFieldExpression) {
            return expressionStack.push(UnknownOwnerFieldExpressionConverter.convert(
                    unknownOwnerFieldExpression, expressionStack.peek().getType(), getFieldInfoProvider
            ));
        }
        if (astExpression instanceof ASTFieldAccessExpression fieldAccessExpression) {
            Optional<OxmaFieldInfo> fieldInfoO = fieldInfoProvider.apply(fieldAccessExpression.getFieldName());
            if (fieldInfoO.isEmpty()) {
                throw new IllegalArgumentException("Cannot find declaration of " + fieldAccessExpression.getFieldName());
            }
            OxmaFieldInfo fieldInfo = fieldInfoO.get();
            return expressionStack.push(new FieldAccessExpression(
                    fieldInfo.type(),
                    fieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC),
                    fieldInfo.ownerClassName(),
                    fieldAccessExpression.getFieldName()
            ));
        }
        if (astExpression instanceof ASTUnknownOwnerMethodCall unknownOwnerMethodCall) {
            return expressionStack.push(UnknownOwnerMethodCallExpressionConverter.convert(
                    unknownOwnerMethodCall,
                    expressionStack.peek().getType(),
                    getFunctionInfoProvider,
                    (expr, funInfoProvider) -> convert(expr, getVariableOrField, funInfoProvider, fieldInfoProvider)));
        }
        if (astExpression instanceof ASTClassPropertyCallExpression propertyCallExpression) {
            return new ClassPropertyCallExpression(
                    propertyCallExpression.getExpressions().stream()
                            .map(expr -> convert(expr, getVariableOrField, functionInfoProvider, fieldInfoProvider))
                            .toList()
            );
        }
        if (astExpression instanceof ASTThisExpression) {
            return new ThisExpression();
        }

        logger.error("Unknown expression {}", astExpression);
        throw new IllegalArgumentException("Unexpected expression type");
    }

    private AbstractFunCallExpression tryToFindStdFun(String funName, List<Expression> arguments) {
        Optional<Class<? extends AbstractFunCallExpression>> stdFun = ODKMapper.getO(funName);
        if (stdFun.isEmpty()) {
            logger.error("Unknown fun reference {}", funName);
            throw new IllegalArgumentException("Unknown fun reference");
        }
        try {
            return stdFun.get().getConstructor(List.class).newInstance(arguments);
        } catch (Exception e) {
            logger.error("Cannot call constructor of the standard fun {}", funName);
            throw new IllegalArgumentException("Internal error. Cannot call constructor of the standard fun " + funName);
        }
    }

    public CondExpression convertCondExpression(
            ASTCondExpression condExpression,
            AbstractLocalVariableTable localVariableTable,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider) throws CFGConversionException
    {
        return convertCondExpression(
                condExpression,
                new GetVariableOrField(localVariableTable, fieldInfoProvider),
                functionInfoProvider,
                fieldInfoProvider
        );
    }

    public CondExpression convertCondExpression(
            ASTCondExpression condExpression,
            GetVariableOrField getVariableOrField,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider) throws CFGConversionException
    {
        List<Expression> expressions = condExpression.expressions().stream()
                .map(expr -> convert(expr, getVariableOrField, functionInfoProvider, fieldInfoProvider))
                .toList();
        try {
            Validator.validateConditionExpression(CFGUtils.getSignatureFromExpressions(expressions));
        } catch (IncompatibleTypesException e) {
            throw new CFGConversionException("Incompatible types", e);
        }
        return new CondExpression(expressions, condExpression.operations());
    }

    protected abstract void validateStaticContext(OxmaFunctionInfo functionInfo, String funName);
}