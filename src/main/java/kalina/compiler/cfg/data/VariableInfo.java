package kalina.compiler.cfg.data;

import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class VariableInfo {
    private final String name;
    private final int index;
    private final Type type;
    private final Optional<AssignArrayVariableInfo> arrayVariableInfo;
    private final SSAVariableInfo ssaVariableInfo;

    public VariableInfo(String name, int index, Type type) {
        this.name = name;
        this.index = index;
        this.type = type;
        this.arrayVariableInfo = Optional.empty();
        ssaVariableInfo = new SSAVariableInfo(name);
    }

    public VariableInfo(String name, int index, Type type, AssignArrayVariableInfo arrayVariableInfo) {
        this.name = name;
        this.index = index;
        this.type = type;
        this.arrayVariableInfo = Optional.of(arrayVariableInfo);
        ssaVariableInfo = new SSAVariableInfo(name);
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public Type getType() {
        return type;
    }

    public AssignArrayVariableInfo getArrayVariableInfoOrElseThrow() {
        return arrayVariableInfo.orElseThrow();
    }

    @Override
    public String toString() {
        return type.getClassName() + " " + name + "_" + ssaVariableInfo.getCfgIndex();
    }

    public SSAVariableInfo getSsaVariableInfo() {
        return ssaVariableInfo;
    }
}
