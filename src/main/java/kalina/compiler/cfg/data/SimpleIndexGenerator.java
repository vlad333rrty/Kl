package kalina.compiler.cfg.data;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class SimpleIndexGenerator implements IndexGenerator {
    private int index;

    public SimpleIndexGenerator(int start) {
        this.index = start;
    }

    @Override
    public int getNewIndex(Type type) {
        int result = index;
        index += getStep(type);
        return result;
    }

    private int getStep(Type type) {
        return type.getSort() == Type.DOUBLE || type.getSort() == Type.LONG ? 2 : 1;
    }
}
