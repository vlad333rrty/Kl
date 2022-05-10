package kalina.compiler.ast.expression.array;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public record ASTArrayLHS(String name, List<ASTExpression> indices) {
    @Override
    public String toString() {
        return name + indices;
    }
}
