package kalina.compiler.ast.expression.array;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.expressions.Expression;

/**
 * @author vlad333rrty
 */
public class ASTArrayCreationAndInitializationExpression implements ASTExpression {
    private final List<List<Expression>> defaultValues;

    public ASTArrayCreationAndInitializationExpression(List<List<Expression>> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public List<List<Expression>> getDefaultValues() {
        return defaultValues;
    }
}
