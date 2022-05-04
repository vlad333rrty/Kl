package kalina.compiler.ast.expression;

/**
 * @author vlad333rrty
 */
public record ASTVariableExpression(String name) implements ASTExpression {
    @Override
    public String toString() {
        return name;
    }
}
