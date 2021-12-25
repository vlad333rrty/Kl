package kalina.compiler.syntax.parser.data;

/**
 * @author vlad333rrty
 */
public interface ITypeDictionary {
    boolean addType(String name);

    boolean hasType(String name);

    boolean isPrimitive(String name);
}
