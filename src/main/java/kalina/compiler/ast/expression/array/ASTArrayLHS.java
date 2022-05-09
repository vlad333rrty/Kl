package kalina.compiler.ast.expression.array;

import java.util.List;

/**
 * @author vlad333rrty
 */
public record ASTArrayLHS(String name, List<Integer> indices) {
    @Override
    public String toString() {
        return name + indices;
    }
}
