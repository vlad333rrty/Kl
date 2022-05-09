package kalina.compiler.ast.expression;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vlad333rrty
 */
public abstract class ASTAbstractAssignInstruction implements ASTExpression {
    private final List<ASTExpression> rhs;

    public ASTAbstractAssignInstruction(List<ASTExpression> rhs) {
        this.rhs = rhs;
    }

    public List<ASTExpression> getRHS() {
        return rhs;
    }

    @Override
    public String toString() {
        return getLHSString() + " = " + rhs.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    protected abstract String getLHSString();
}
