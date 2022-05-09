package kalina.compiler.ast.expression.array;

import java.util.List;

import kalina.compiler.ast.expression.ASTAbstractAssignInstruction;
import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public final class ASTArrayAssignInstruction extends ASTAbstractAssignInstruction {
    private final List<ASTArrayLHS> lhs;

    public ASTArrayAssignInstruction(List<ASTArrayLHS> lhs, List<ASTExpression> rhs) {
        super(rhs);
        this.lhs = lhs;
    }

    public List<ASTArrayLHS> getLHS() {
        return lhs;
    }

    @Override
    protected String getLHSString() {
        return lhs.toString();
    }
}
