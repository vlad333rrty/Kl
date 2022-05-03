package kalina.compiler.ast.expression;

/**
 * @author vlad333rrty
 */
public record ASTFunEndInstruction() implements ASTExpression {
    @Override
    public String toString() {
        return "FUN END";
    }
}
