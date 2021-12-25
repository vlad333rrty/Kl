package kalina.compiler.bb;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class TypeAndName {
    private final Type type;
    private final String name;

    public TypeAndName(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
