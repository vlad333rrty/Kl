package kalina.compiler.ast.expression;

/**
 * @author vlad333rrty
 */
public record ASTFactor(ASTExpression expression, boolean shouldNegate) implements ASTExpression {

    public static ASTFactor createFactor(ASTExpression expression) {
        return new ASTFactor(expression, false);
    }

    public static ASTFactor createNegateFactor(ASTExpression expression) {
        return new ASTFactor(expression, true);
    }

    @Override
    public String toString() {
        return expression.toString();
    }
}
