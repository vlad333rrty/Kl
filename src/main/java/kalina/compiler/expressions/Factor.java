package kalina.compiler.expressions;

import kalina.compiler.codegen.CodeGenException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class Factor extends Expression {
    private final Expression expression;
    private final boolean shouldNegate;

    private Factor(Expression expression, boolean shouldNegate) {
        this.expression = expression;
        this.shouldNegate = shouldNegate;
    }

    public static Factor createFactor(Expression expression) {
        return new Factor(expression, false);
    }

    public static Factor createNegateFactor(Expression expression) {
        return new Factor(expression, true);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        expression.translateToBytecode(mv);
        if (shouldNegate) {
            mv.visitInsn(expression.getType().getOpcode(Opcodes.INEG));
        }
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public String toString() {
        return (shouldNegate ? "-" : "") + expression.toString();
    }

    public Expression getExpression() {
        return expression;
    }

    public Factor withExpression(Expression expression) {
        return new Factor(expression, shouldNegate);
    }

    public boolean shouldNegate() {
        return shouldNegate;
    }
}
