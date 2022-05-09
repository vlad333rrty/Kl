package kalina.compiler.expressions.v2.funCall;

import java.util.List;

import kalina.compiler.expressions.Expression;

/**
 * @author vlad333rrty
 */
public abstract class AbstractFunCallExpression extends Expression {
    protected final List<Expression> arguments;

    public AbstractFunCallExpression(List<Expression> arguments) {
        this.arguments = arguments;
    }
}
