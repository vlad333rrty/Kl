package kalina.compiler.expressions;

import kalina.compiler.cfg.data.SSAVariableInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class VariableExpression extends Expression {
    private final int index;
    private final Type type;

    private final SSAVariableInfo ssaVariableInfo;
    private final String name;

    public VariableExpression(int index, Type type, String name) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.ssaVariableInfo = new SSAVariableInfo(name);
    }

    public VariableExpression(int index, Type type, String name, int cfgIndex) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.ssaVariableInfo = new SSAVariableInfo(name, cfgIndex);
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
        return ssaVariableInfo.toString();
    }

    public String getName() {
        return name;
    }

    public SSAVariableInfo getSsaVariableInfo() {
        return ssaVariableInfo;
    }

    public VariableExpression withCfgIndex(int cfgIndex) {
        return new VariableExpression(index, type, name, cfgIndex);
    }
}
