package kalina.compiler.cfg.data;

import java.util.Set;

/**
 * @author vlad333rrty
 */
public class TypeChecker {
    private static final Set<String> isPrimitiveNumber =
            Set.of("short", "int", "long", "bool", "float", "double");

    private final TypeDictionary typeDictionary;

    public TypeChecker(TypeDictionary typeDictionary) {
        this.typeDictionary = typeDictionary;
    }

    public boolean hasType(String name) {
        return typeDictionary.hasType(name);
    }

    public static boolean isPrimitiveNumber(String name) {
        return isPrimitiveNumber.contains(name);
    }
}
