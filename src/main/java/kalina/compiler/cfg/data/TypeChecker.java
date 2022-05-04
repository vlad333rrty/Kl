package kalina.compiler.cfg.data;

import kalina.compiler.syntax.parser.data.ITypeDictionary;

/**
 * @author vlad333rrty
 */
public class TypeChecker {
    private final ITypeDictionary typeDictionary;

    public TypeChecker(ITypeDictionary typeDictionary) {
        this.typeDictionary = typeDictionary;
    }

    public boolean hasType(String name) {
        return typeDictionary.hasType(name);
    }

    public boolean isPrimitive(String name) {
        return typeDictionary.isPrimitive(name);
    }
}
