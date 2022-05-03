package kalina.compiler.ast.expression;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author vlad333rrty
 */
public final class ASTFunCallExpression implements ASTExpression {
    private final String funName;
    private final List<ASTExpression> arguments;

    public ASTFunCallExpression(String funName, List<ASTExpression> arguments) {
        this.funName = funName;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return funName + "(" + arguments.stream().map(Objects::toString).collect(Collectors.joining(", ")) + ")";
    }

    public String funName() {
        return funName;
    }

    public List<ASTExpression> arguments() {
        return arguments;
    }
}
