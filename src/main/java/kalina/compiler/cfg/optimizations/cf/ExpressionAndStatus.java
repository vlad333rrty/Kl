package kalina.compiler.cfg.optimizations.cf;

import kalina.compiler.expressions.Expression;

/**
 * @author vlad333rrty
 */
record ExpressionAndStatus(Expression expression, Status status, boolean shouldNegate) {
    public enum Status {
        IS_CONSTANT, IS_NOT_CONSTANT
    }
}