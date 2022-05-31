package kalina.compiler.cfg.optimizations;

import java.util.List;
import java.util.function.Function;

import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.array.ArrayGetElementExpression;
import kalina.compiler.expressions.v2.array.ArrayWithCapacityCreationExpression;
import kalina.compiler.expressions.v2.array.FieldArrayGetElementExpression;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;

/**
 * @author vlad333rrty
 */
public class ExpressionSubstitutor {
    public static Expression substituteExpression(
            Expression expression,
            String varName,
            Function<VariableExpression, Expression> variableExpressionMapper,
            Function<ArrayGetElementExpression, ArrayGetElementExpression> arrayGetElementExpressionTransformer)
    {
        if (expression instanceof VariableExpression variableExpression) {
            String name = variableExpression.getName();
            return variableExpressionMapper.apply(variableExpression);
        } else if (expression instanceof ArithmeticExpression arithmeticExpression) {
            List<Term> terms = arithmeticExpression.getTerms().stream()
                    .map(x -> substituteExpression(x, varName, variableExpressionMapper, arrayGetElementExpressionTransformer))
                    .map(x -> (Term) x)
                    .toList();
            return arithmeticExpression.withTerms(terms);
        } else if (expression instanceof Term term) {
            List<Factor> factors = term.getFactors().stream()
                    .map(x -> substituteExpression(x, varName, variableExpressionMapper, arrayGetElementExpressionTransformer))
                    .map(x -> (Factor) x)
                    .toList();
            return term.withFactors(factors);
        } else if (expression instanceof Factor factor) {
            return factor.withExpression(substituteExpression(factor.getExpression(), varName,
                    variableExpressionMapper, arrayGetElementExpressionTransformer));
        } else if (expression instanceof CondExpression condExpression) {
            List<Expression> expressions = condExpression.getExpressions().stream()
                    .map(x -> substituteExpression(x, varName, variableExpressionMapper, arrayGetElementExpressionTransformer))
                    .toList();
            return condExpression.substituteExpressions(expressions);
        } else if (expression instanceof AbstractFunCallExpression funCallExpression) {
            List<Expression> arguments = funCallExpression.getArguments().stream()
                    .map(x -> substituteExpression(x, varName, variableExpressionMapper, arrayGetElementExpressionTransformer))
                    .toList();
            return funCallExpression.substituteArguments(arguments);
        } else if (expression instanceof ArrayGetElementExpression arrayGetElementExpression) {
            List<Expression> indices = arrayGetElementExpression.getIndices().stream()
                    .map(x -> substituteExpression(x, varName, variableExpressionMapper, arrayGetElementExpressionTransformer))
                    .toList();
            String name = arrayGetElementExpression.getName();
            if (name.equals(varName)) {
                ArrayGetElementExpression transformedExpression = arrayGetElementExpressionTransformer
                        .apply(arrayGetElementExpression);
                return transformedExpression.substituteExpressions(indices);
            }
            return arrayGetElementExpression.substituteExpressions(indices);
        } else if (expression instanceof ArrayWithCapacityCreationExpression arrayWithCapacityCreationExpression) {
            List<Expression> capacities = arrayWithCapacityCreationExpression.getCapacities().stream()
                    .map(x -> substituteExpression(x, varName, variableExpressionMapper, arrayGetElementExpressionTransformer))
                    .toList();
            return arrayWithCapacityCreationExpression.substituteExpressions(capacities);
        } else if (expression instanceof FieldArrayGetElementExpression fieldArrayGetElementExpression) {
            List<Expression> indices = fieldArrayGetElementExpression.getIndices().stream()
                    .map(x -> substituteExpression(x, varName, variableExpressionMapper, arrayGetElementExpressionTransformer))
                    .toList();
            return fieldArrayGetElementExpression.substituteExpressions(indices);
        }

        return expression;
    }
}
