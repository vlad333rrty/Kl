package kalina.compiler.ast.expression;

import java.util.Optional;

/**
 * @author vlad333rrty
 */
public class ASTReturnInstruction implements ASTExpression {
    private final Optional<ASTExpression> returnExpression;

    public ASTReturnInstruction(Optional<ASTExpression> returnExpression) {
        this.returnExpression = returnExpression;
    }

    @Override
    public String toString() {
        return "ret " + (returnExpression.isPresent() ? returnExpression.get().toString() : "");
    }
}
