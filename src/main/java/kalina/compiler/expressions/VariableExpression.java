package kalina.compiler.expressions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class VariableExpression extends Expression {
    private final int index;
    private final Type type;

    public VariableExpression(int index, Type type) {
        this.index = index;
        this.type = type;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) {
        expressionCodeGen.loadVariable(mv, type.getOpcode(Opcodes.ILOAD), index);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "variable with index " + index;
    }
}
