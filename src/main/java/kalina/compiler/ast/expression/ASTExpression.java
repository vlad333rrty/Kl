package kalina.compiler.ast.expression;

import java.util.List;

/**
 * @author vlad333rrty
 */
public interface ASTExpression {

    default String complexExpressionToString(List<?> elements, List<?> operations) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                builder.append(operations.get(i - 1)).append(" ");
            }
            builder.append(elements.get(i).toString()).append(" ");
        }
        return builder.toString();
    }
}
