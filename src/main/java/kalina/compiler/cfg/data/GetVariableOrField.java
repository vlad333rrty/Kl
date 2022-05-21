package kalina.compiler.cfg.data;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author vlad333rrty
 */
public class GetVariableOrField {
    private final AbstractLocalVariableTable localVariableTable;
    private final Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider;

    public GetVariableOrField(
            AbstractLocalVariableTable localVariableTable,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        this.localVariableTable = localVariableTable;
        this.fieldInfoProvider = fieldInfoProvider;
    }

    public Optional<VariableOrFieldInfo> getVariableOrFieldInfo(String name) {
        Optional<TypeAndIndex> typeAndIndex = localVariableTable.findVariable(name);
        if (typeAndIndex.isPresent()) {
            return Optional.of(new VariableOrFieldInfo(typeAndIndex.get()));
        }

        Optional<OxmaFieldInfo> fieldInfoO = fieldInfoProvider.apply(name);
        return fieldInfoO.map(VariableOrFieldInfo::new);
    }

    public VariableOrFieldInfo getVariableOrFieldInfoOrElseThrow(String name) {
        Optional<VariableOrFieldInfo> infoO =  getVariableOrFieldInfo(name);
        if (infoO.isPresent()) {
            return infoO.get();
        }

        throw new IllegalArgumentException("No declaration found for " + name);
    }


    public static final class VariableOrFieldInfo {
        public final Optional<TypeAndIndex> typeAndIndex;
        public final Optional<OxmaFieldInfo> fieldInfo;

        public VariableOrFieldInfo(TypeAndIndex typeAndIndex) {
            this.typeAndIndex = Optional.of(typeAndIndex);
            this.fieldInfo = Optional.empty();
        }

        public VariableOrFieldInfo(OxmaFieldInfo fieldInfo) {
            this.typeAndIndex = Optional.empty();
            this.fieldInfo = Optional.of(fieldInfo);
        }
    }
}
