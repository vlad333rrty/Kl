package kalina.compiler.ast.expression;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public record ASTValueExpression(Object value, Type type) implements ASTExpression {
    @Override
    public String toString() {
        return type.getClassName() + " " + value;
    }
}
