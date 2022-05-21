package kalina.compiler.ast.expression.method;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public class ASTUnknownOwnerMethodCall implements ASTExpression {
    private final String funName;
    private final List<ASTExpression> arguments;

    public ASTUnknownOwnerMethodCall(String funName, List<ASTExpression> arguments) {
        this.funName = funName;
        this.arguments = arguments;
    }

    public String getFunName() {
        return funName;
    }

    public List<ASTExpression> getArguments() {
        return arguments;
    }

    public String toString() {
        return "[UNKNOWN]" + "." + funName + "(" + arguments + ")";
    }
}
