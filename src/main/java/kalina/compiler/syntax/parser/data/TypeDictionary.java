package kalina.compiler.syntax.parser.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author vlad333rrty
 */
public class TypeDictionary implements ITypeDictionary {
    private static final Set<String> primitiveTypes =
            new HashSet<>(Arrays.asList("char", "short", "int", "long", "bool", "string", "float", "double", "void"));
    private static final Set<String> types = new HashSet<>();

    @Override
    public boolean addType(String name) {
        return !hasType(name) && types.add(name);
    }

    @Override
    public boolean hasType(String name) {
        return isPrimitive(name) || types.contains(name);
    }

    @Override
    public boolean isPrimitive(String name) {
        return primitiveTypes.contains(name);
    }
}
