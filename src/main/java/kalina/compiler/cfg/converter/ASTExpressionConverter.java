package kalina.compiler.cfg.converter;

import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.expression.ASTArithmeticExpression;
import kalina.compiler.ast.expression.ASTCondExpression;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTFactor;
import kalina.compiler.ast.expression.ASTFunCallExpression;
import kalina.compiler.ast.expression.ASTMethodCallExpression;
import kalina.compiler.ast.expression.ASTObjectCreationExpression;
import kalina.compiler.ast.expression.ASTTerm;
import kalina.compiler.ast.expression.ASTValueExpression;
import kalina.compiler.ast.expression.ASTVariableExpression;
import kalina.compiler.ast.expression.array.ASTArrayCreationExpression;
import kalina.compiler.ast.expression.array.ASTArrayGetElementExpression;
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.traverse.GetFunctionInfoProvider;
import kalina.compiler.cfg.traverse.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.cfg.validator.Validator;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.ObjectCreationExpression;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.array.ArrayGetElementExpression;
import kalina.compiler.expressions.v2.array.ArrayWithCapacityCreationExpression;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import kalina.compiler.expressions.v2.funCall.FunCallExpression;
import kalina.compiler.odk.ODKMapper;
import kalina.compiler.syntax.parser.data.AbstractLocalVariableTable;
import kalina.compiler.syntax.parser.data.TypeAndIndex;
import kalina.compiler.syntax.parser2.data.OxmaFunctionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ASTExpressionConverter {
    private static final Logger logger = LogManager.getLogger(ASTExpressionConverter.class);

    private final GetFunctionInfoProvider getFunctionInfoProvider;

    public ASTExpressionConverter(GetFunctionInfoProvider getFunctionInfoProvider) {
        this.getFunctionInfoProvider = getFunctionInfoProvider;
    }

    public Expression convert(
            ASTExpression astExpression,
            AbstractLocalVariableTable localVariableTable,
            OxmaFunctionInfoProvider functionInfoProvider)
    {
        if (astExpression instanceof ASTValueExpression valueExpression) {
            return new ValueExpression(valueExpression.value(), valueExpression.type());
        }
        if (astExpression instanceof ASTVariableExpression variableExpression) {
            TypeAndIndex typeAndIndex = localVariableTable.findVariableOrElseThrow(variableExpression.name());
            return new VariableExpression(typeAndIndex.getIndex(), typeAndIndex.getType());
        }
        if (astExpression instanceof ASTFactor factor) {
            Expression expression = convert(factor.expression(), localVariableTable, functionInfoProvider);
            return factor.shouldNegate() ? Factor.createNegateFactor(expression) : Factor.createFactor(expression);
        }
        if (astExpression instanceof ASTTerm term) {
            List<Factor> factors = term.factors().stream()
                    .map(factor -> (Factor)convert(factor, localVariableTable, functionInfoProvider)).toList();
            return new Term(factors, term.operations());
        }
        if (astExpression instanceof ASTArithmeticExpression arithmeticExpression) {
            List<Term> terms = arithmeticExpression.terms().stream()
                    .map(term -> (Term)convert(term, localVariableTable, functionInfoProvider)).toList();
            return new ArithmeticExpression(terms, arithmeticExpression.operations());
        }
        if (astExpression instanceof ASTFunCallExpression funCallExpression) {
            List<Expression> arguments = funCallExpression.arguments().stream()
                    .map(arg -> convert(arg, localVariableTable, functionInfoProvider))
                    .toList();
            String funName = funCallExpression.funName();
            Optional<OxmaFunctionInfo> functionInfoO = functionInfoProvider
                    .getFunctionInfo(funName, getSignatureFromExpressions(arguments));
            if (functionInfoO.isEmpty()) {
                return tryToFindStdFun(funName, arguments);
            }
            OxmaFunctionInfo functionInfo = functionInfoO.get();
            return new FunCallExpression(funName, arguments, functionInfo, functionInfo.isStatic() ? Optional.empty() : Optional.of(0));
        }
        if (astExpression instanceof ASTObjectCreationExpression objectCreationExpression) {
            List<Expression> arguments = objectCreationExpression.arguments().stream()
                    .map(arg -> convert(arg, localVariableTable, functionInfoProvider))
                    .toList();
            return new ObjectCreationExpression(objectCreationExpression.className(), arguments);
        }
        if (astExpression instanceof ASTMethodCallExpression methodCallExpression) {
            String ownerObjectName = methodCallExpression.ownerObjectName();
            Optional<TypeAndIndex> variableO = localVariableTable.findVariable(ownerObjectName);
            Optional<OxmaFunctionInfoProvider> otherClassFunctionTable = variableO.isPresent()
                    ? getFunctionInfoProvider.getFunctionTable(variableO.get().getType().getClassName())
                    : getFunctionInfoProvider.getFunctionTable(ownerObjectName);
            if (otherClassFunctionTable.isEmpty()) {
                logger.error("Unknown type {}", methodCallExpression.ownerObjectName());
                throw new IllegalArgumentException("No function info can be found for " + methodCallExpression.funName());
            }
            return convertFunCallExpression(
                    methodCallExpression.funName(),
                    methodCallExpression.arguments(),
                    localVariableTable,
                    otherClassFunctionTable.get(),
                    variableO.map(TypeAndIndex::getIndex));
        }
        if (astExpression instanceof ASTArrayCreationExpression arrayCreationExpression) {
            List<Expression> capacities = arrayCreationExpression.getCapacities().stream()
                    .map(e -> convert(e, localVariableTable, functionInfoProvider))
                    .toList();
            for (Expression capacity : capacities) {
                if (capacity.getType() != Type.INT_TYPE) {
                    throw new IllegalArgumentException("Unexpected type used for array creation: " + capacity.getType().getClassName());
                }
            }
            return new ArrayWithCapacityCreationExpression(
                    arrayCreationExpression.getCapacities().stream().map(e -> convert(e, localVariableTable, functionInfoProvider)).toList(),
                    arrayCreationExpression.getArrayType(),
                    arrayCreationExpression.getElementType());
        }
        if (astExpression instanceof ASTArrayGetElementExpression getElementExpression) {
            TypeAndIndex variableInfo =
                    localVariableTable.findVariableOrElseThrow(getElementExpression.getVariableName());
            return new ArrayGetElementExpression(
                    getElementExpression.getIndices().stream().map(expr -> convert(expr, localVariableTable, functionInfoProvider)).toList(),
                    CFGUtils.getArrayElementType(variableInfo.getType()),
                    CFGUtils.lowArrayDimension(variableInfo.getType(), getElementExpression.getIndices().size()),
                    variableInfo.getType(),
                    variableInfo.getIndex()
            );
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

    private FunCallExpression convertFunCallExpression(
            String funName,
            List<ASTExpression> astArgs,
            AbstractLocalVariableTable localVariableTable,
            OxmaFunctionInfoProvider functionTable,
            Optional<Integer> variableIndex)
    {
        List<Expression> arguments = astArgs.stream()
                .map(arg -> convert(arg, localVariableTable, functionTable))
                .toList();
        Optional<OxmaFunctionInfo> functionInfo = functionTable
                .getFunctionInfo(funName, getSignatureFromExpressions(arguments));
        if (functionInfo.isEmpty()) {
            logger.error("No declaration found for method {}", funName);
            throw new IllegalArgumentException("No declaration found for method " + funName);
        }
        return new FunCallExpression(funName, arguments, functionInfo.get(), variableIndex);
    }

    public CondExpression convertCondExpression(
            ASTCondExpression condExpression,
            AbstractLocalVariableTable localVariableTable,
            OxmaFunctionInfoProvider functionTable) throws CFGConversionException
    {
        List<Expression> expressions = condExpression.expressions().stream()
                .map(expr -> convert(expr, localVariableTable, functionTable))
                .toList();
        try {
            Validator.validateConditionExpression(getSignatureFromExpressions(expressions));
        } catch (IncompatibleTypesException e) {
            throw new CFGConversionException("Incompatible types", e);
        }
        return new CondExpression(expressions, condExpression.operations());
    }

    private List<Type> getSignatureFromExpressions(List<Expression> expressions) {
        return expressions.stream().map(Expression::getType).toList();
    }
}
