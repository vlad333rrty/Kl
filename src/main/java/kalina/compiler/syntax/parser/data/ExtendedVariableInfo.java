package kalina.compiler.syntax.parser.data;

import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ExtendedVariableInfo {
    private final Type type;
    private final int index;
    private Optional<ArrayVariableInfo> arrayVariableInfo;

    public ExtendedVariableInfo(Type type, int index) {
        this.type = type;
        this.index = index;
        this.arrayVariableInfo = Optional.empty();
    }

    public ExtendedVariableInfo(Type type, int index, ArrayVariableInfo arrayVariableInfo) {
        this.type = type;
        this.index = index;
        this.arrayVariableInfo = Optional.of(arrayVariableInfo);
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public Optional<ArrayVariableInfo> getArrayVariableInfo() {
        return arrayVariableInfo;
    }

    public void setArrayVariableInfo(ArrayVariableInfo arrayVariableInfo) {
        this.arrayVariableInfo = Optional.of(arrayVariableInfo);
    }
}
