package kalina.compiler.ast.expression;

import java.util.List;

/**
 * @author vlad333rrty
 */
public class ASTClassPropertyCallExpression implements ASTExpression {
    private final List<ASTExpression> expressions;

    public ASTClassPropertyCallExpression(List<ASTExpression> expressions) {
        this.expressions = expressions;
    }

    public List<ASTExpression> getExpressions() {
        return expressions;
    }
}
