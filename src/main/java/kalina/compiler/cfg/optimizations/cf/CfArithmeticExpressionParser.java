package kalina.compiler.cfg.optimizations.cf;

import kalina.compiler.cfg.optimizations.ConstantExpressionDetector;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.ValueExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class CfArithmeticExpressionParser {
    private static final Logger logger = LogManager.getLogger(CfArithmeticExpressionParser.class);

    public static Expression parseExpression(Expression expression) {
        if (expression instanceof ValueExpression) {
            return expression;
        }
        if (expression instanceof ArithmeticExpression arithmeticExpression) {
            if (ConstantExpressionDetector.isNumberConstant(arithmeticExpression)) {
                try {
                    Number number = NumberCalculator.parseAr(arithmeticExpression);
                    return new ValueExpression(number, arithmeticExpression.getType());
                } catch (ArithmeticException | IncompatibleTypesException e) {
                    logger.warn(e);
                }
            }
            return arithmeticExpression;
        }
        return expression;
    }
}
