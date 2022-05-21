package kalina.compiler.ast.expression.method;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public final class ASTMethodCallExpression implements ASTExpression {
    private final String ownerObjectName;
    private final String funName;
    private final List<ASTExpression> arguments;

    public ASTMethodCallExpression(
            String ownerObjectName,
            String funName,
            List<ASTExpression> arguments)
    {
        this.ownerObjectName = ownerObjectName;
        this.funName = funName;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return ownerObjectName + "." + funName + "(" + arguments + ")";
    }

    public String ownerObjectName() {
        return ownerObjectName;
    }

    public String funName() {
        return funName;
    }

    public List<ASTExpression> arguments() {
        return arguments;
    }
}
