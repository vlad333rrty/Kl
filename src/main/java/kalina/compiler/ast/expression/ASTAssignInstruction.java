package kalina.compiler.ast.expression;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vlad333rrty
 */
public class ASTAssignInstruction implements ASTExpression {
    private final List<String> lhs;
    private final List<ASTExpression> rhs;

    public ASTAssignInstruction(List<String> lhs, List<ASTExpression> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return lhs + " = " + rhs.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    public List<String> lhs() {
        return lhs;
    }

    public List<ASTExpression> rhs() {
        return rhs;
    }
}
