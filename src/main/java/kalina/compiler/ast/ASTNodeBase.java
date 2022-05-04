package kalina.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public abstract class ASTNodeBase implements ASTNode {
    private final List<ASTNode> children = new ArrayList<>();
    private final List<ASTExpression> expressions = new ArrayList<>();

    @Override
    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(ASTNode child) {
        children.add(child);
    }

    @Override
    public List<ASTExpression> getExpressions() {
        return expressions;
    }

    @Override
    public void addExpression(ASTExpression expression) {
        expressions.add(expression);
    }
}
