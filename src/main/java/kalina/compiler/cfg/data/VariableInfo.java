package kalina.compiler.cfg.data;

import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class VariableInfo implements WithIR {
    private final String name;
    private final Optional<Integer> index;
    private final Type type;
    private final Optional<AssignArrayVariableInfo> arrayVariableInfo;
    private final Optional<OxmaFieldInfo> fieldInfo;
    private final SSAVariableInfo ssaVariableInfo;

    public VariableInfo(String name, int index, Type type) {
        this.name = name;
        this.index = Optional.of(index);
        this.type = type;
        this.arrayVariableInfo = Optional.empty();
        this.fieldInfo = Optional.empty();
        ssaVariableInfo = new SSAVariableInfo(name);
    }

    public VariableInfo(String name, Type type, OxmaFieldInfo fieldInfo) {
        this.name = name;
        this.index = Optional.empty();
        this.type = type;
        this.arrayVariableInfo = Optional.empty();
        this.fieldInfo = Optional.of(fieldInfo);
        ssaVariableInfo = new SSAVariableInfo(name);
    }

    public VariableInfo(String name, int index, Type type, AssignArrayVariableInfo arrayVariableInfo) {
        this.name = name;
        this.index = Optional.of(index);
        this.type = type;
        this.arrayVariableInfo = Optional.of(arrayVariableInfo);
        this.fieldInfo = Optional.empty();
        ssaVariableInfo = new SSAVariableInfo(name);
    }

    public VariableInfo(String name, Type type, AssignArrayVariableInfo arrayVariableInfo, OxmaFieldInfo fieldInfo) {
        this.name = name;
        this.index = Optional.empty();
        this.type = type;
        this.arrayVariableInfo = Optional.of(arrayVariableInfo);
        this.fieldInfo = Optional.of(fieldInfo);
        ssaVariableInfo = new SSAVariableInfo(name);
    }

    public String getName() {
        return name;
    }

    public int getIndexOrElseThrow() {
        return index.orElseThrow();
    }

    public Type getType() {
        return type;
    }

    public AssignArrayVariableInfo getArrayVariableInfoOrElseThrow() {
        return arrayVariableInfo.orElseThrow();
    }

    public Optional<OxmaFieldInfo> getFieldInfo() {
        return fieldInfo;
    }

    @Override
    public String toString() {
        return type.getClassName() + " " + name + "_" + ssaVariableInfo.getCfgIndex();
    }

    public SSAVariableInfo getSsaVariableInfo() {
        return ssaVariableInfo;
    }

    @Override
    public String getIR() {
        return ssaVariableInfo.getIR();
    }
}
