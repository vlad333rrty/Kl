package kalina.compiler.expressions;

import java.util.List;

/**
 * @author vlad333rrty
 */
public class RHS {
    private final List<Expression> expressions;

    public RHS(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }
}
