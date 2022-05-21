package kalina.compiler.cfg.data;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class TypeAndIndex {
    private final Type type;
    private final int index;

    public TypeAndIndex(Type type, int index) {
        this.type = type;
        this.index = index;
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }
}
