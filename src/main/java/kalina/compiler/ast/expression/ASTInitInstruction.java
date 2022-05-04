package kalina.compiler.ast.expression;

import java.util.List;
import java.util.stream.Collectors;

import kalina.compiler.ast.ASTLHS;

/**
 * @author vlad333rrty
 */
public class ASTInitInstruction implements ASTExpression {
    private final ASTLHS lhs;
    private final List<ASTExpression> rhs;

    public ASTInitInstruction(ASTLHS lhs, List<ASTExpression> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return lhs + " = " + rhs.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    public ASTLHS lhs() {
        return lhs;
    }

    public List<ASTExpression> rhs() {
        return rhs;
    }
}
