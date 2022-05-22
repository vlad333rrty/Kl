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

    private final String name;
    private final int cfgIndex;

    public VariableExpression(int index, Type type, String name) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.cfgIndex = 0;
    }

    public VariableExpression(int index, Type type, String name, int cfgIndex) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.cfgIndex = cfgIndex;
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
        return name + "_" + cfgIndex;
    }

    public String getName() {
        return name;
    }

    public VariableExpression withCfgIndex(int cfgIndex) {
        return new VariableExpression(index, type, name, cfgIndex);
    }
}
