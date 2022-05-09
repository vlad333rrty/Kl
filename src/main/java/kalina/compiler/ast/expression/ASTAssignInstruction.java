package kalina.compiler.ast.expression;

import java.util.List;

/**
 * @author vlad333rrty
 */
public class ASTAssignInstruction extends ASTAbstractAssignInstruction {
    private final List<String> lhs;

    public ASTAssignInstruction(List<String> lhs, List<ASTExpression> rhs) {
        super(rhs);
        this.lhs = lhs;
    }

    @Override
    protected String getLHSString() {
        return lhs.toString();
    }

    public List<String> getLHS() {
        return lhs;
    }
}
