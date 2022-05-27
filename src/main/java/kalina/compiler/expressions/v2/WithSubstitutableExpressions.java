package kalina.compiler.expressions.v2;

import java.util.List;

import kalina.compiler.expressions.Expression;

/**
 * @author vlad333rrty
 */
public interface WithSubstitutableExpressions<T extends Expression> {
    Expression substituteExpressions(List<T> expressions);
}
