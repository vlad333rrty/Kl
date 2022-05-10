package kalina.compiler.cfg.data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author vlad333rrty
 */
public class TypeDictionaryImpl implements TypeDictionary {
    private final Set<String> types = new HashSet<>(Set.of(
            "short", "int", "long", "bool", "float", "double", "java.lang.String", "boolean"
    ));

    @Override
    public void addType(String type) {
        if (hasType(type)) {
            throw new IllegalArgumentException("The type is already existed " + type);
        }
        types.add(type);
    }

    @Override
    public boolean hasType(String type) {
        if (isArray(type)) {
            String arrayType = type.substring(0, type.indexOf("["));
            return types.contains(arrayType);
        }
        return types.contains(type);
    }

    private boolean isArray(String type) {
        return type.endsWith("[]");
    }

}
