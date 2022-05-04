package kalina.compiler.ast;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public final class ASTEmptyNode implements ASTNode {
    public static final ASTEmptyNode INSTANCE = new ASTEmptyNode();

    private ASTEmptyNode(){}

    @Override
    public String toString() {
        return "empty";
    }

    @Override
    public List<ASTNode> getChildren() {
        return List.of();
    }

    @Override
    public void addChild(ASTNode child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ASTExpression> getExpressions() {
        return List.of();
    }

    @Override
    public void addExpression(ASTExpression expression) {
        throw new UnsupportedOperationException();
    }
}
