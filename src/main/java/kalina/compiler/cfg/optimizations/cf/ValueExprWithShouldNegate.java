package kalina.compiler.cfg.optimizations.cf;

import kalina.compiler.expressions.ValueExpression;

/**
 * @author vlad333rrty
 */
record ValueExprWithShouldNegate(ValueExpression valueExpression, boolean shouldNegate) {
}