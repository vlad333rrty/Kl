package kalina.compiler.syntax.parser.data;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class VariableInfo {
    private final String name;
    private final int index;
    private final Type type;

    public VariableInfo(String name, int index, Type type) {
        this.name = name;
        this.index = index;
        this.type = type;
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
}
