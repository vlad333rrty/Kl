package kalina.compiler.ast;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public interface ASTNode {
    List<ASTNode> getChildren();
    void addChild(ASTNode child);
    List<ASTExpression> getExpressions();
    void addExpression(ASTExpression expression);
}
