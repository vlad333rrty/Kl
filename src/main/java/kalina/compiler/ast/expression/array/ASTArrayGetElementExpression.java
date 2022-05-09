package kalina.compiler.ast.expression.array;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public class ASTArrayGetElementExpression implements ASTExpression {
    private final String variableName;
    private final List<Integer> indices;

    public ASTArrayGetElementExpression(String variableName, List<Integer> indices) {
        this.variableName = variableName;
        this.indices = indices;
    }

    public String getVariableName() {
        return variableName;
    }

    public List<Integer> getIndices() {
        return indices;
    }
}
