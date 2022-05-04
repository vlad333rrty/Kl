package kalina.compiler.ast;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public class ASTMethodEntryNode extends ASTNodeBase {
    @Override
    public String toString() {
        return "{\n" + expressionsToString() + " }";
    }

    private String expressionsToString() {
        StringBuilder builder = new StringBuilder();
        for (ASTExpression expression : getExpressions()) {
            builder.append("\t").append(expression).append("\n");
        }
        return builder.toString();
    }
}
