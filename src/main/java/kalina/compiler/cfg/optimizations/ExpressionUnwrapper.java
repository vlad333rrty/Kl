package kalina.compiler.cfg.optimizations;

import java.util.function.Consumer;

import kalina.compiler.cfg.data.WithIR;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.ArrayElementAssignExpression;
import kalina.compiler.expressions.v2.ClassPropertyCallExpression;
import kalina.compiler.expressions.v2.array.ArrayGetElementExpression;
import kalina.compiler.expressions.v2.array.ArrayWithCapacityCreationExpression;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class ExpressionUnwrapper {
    private static final Logger logger = LogManager.getLogger(ExpressionUnwrapper.class);

    public static void unwrapExpression(Expression expression, Consumer<WithIR> expressionConsumer) {
        if (expression instanceof VariableExpression variableExpression) {
            expressionConsumer.accept(variableExpression.getSsaVariableInfo());
        } else if (expression instanceof ArithmeticExpression arithmeticExpression) {
            arithmeticExpression.getTerms().forEach(x -> unwrapExpression(x, expressionConsumer));
        } else if (expression instanceof Term term) {
            term.getFactors().forEach(x -> unwrapExpression(x, expressionConsumer));
        } else if(expression instanceof Factor factor) {
            unwrapExpression(factor.getExpression(), expressionConsumer);
        } else if (expression instanceof AbstractFunCallExpression funCallExpression) {
            funCallExpression.getArguments().forEach(x -> unwrapExpression(x, expressionConsumer));
        } else if (expression instanceof CondExpression condExpression) {
            condExpression.getExpressions().forEach(x -> unwrapExpression(x, expressionConsumer));
        } else if (expression instanceof ArrayGetElementExpression arrayGetElementExpression) {
            expressionConsumer.accept(arrayGetElementExpression);
            arrayGetElementExpression.getIndices().forEach(x -> unwrapExpression(x, expressionConsumer));
        } else if (expression instanceof ClassPropertyCallExpression propertyCallExpression) {
            propertyCallExpression.getExpressions().forEach(x -> unwrapExpression(x, expressionConsumer));
        } else if (expression instanceof ArrayElementAssignExpression arrayElementAssignExpression) {
            expressionConsumer.accept(arrayElementAssignExpression);
        } else if (expression instanceof ArrayWithCapacityCreationExpression arrayWithCapacityCreationExpression) {
            arrayWithCapacityCreationExpression.getCapacities().
                    forEach(x -> unwrapExpression(x, expressionConsumer));
        }
        else {
            logger.warn("No suitable expression found for further unwrapping {}", expression);
        }
    }
}
