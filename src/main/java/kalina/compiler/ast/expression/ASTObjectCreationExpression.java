package kalina.compiler.ast.expression;

import java.util.List;

/**
 * @author vlad333rrty
 */
public record ASTObjectCreationExpression(String className, List<ASTExpression> arguments) implements ASTExpression {
    @Override
    public String toString() {
        return "new " + className + "(" + arguments + ")";
    }
}
