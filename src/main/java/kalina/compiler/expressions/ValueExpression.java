package kalina.compiler.expressions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ValueExpression extends Expression {
    private final Object value;
    private final Type type;

    public ValueExpression(Object value, Type type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) {
        expressionCodeGen.putValueOnStack(mv, value);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public Object getValue() {
        return value;
    }
}
