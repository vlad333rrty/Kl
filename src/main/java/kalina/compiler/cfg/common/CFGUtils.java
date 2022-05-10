package kalina.compiler.cfg.common;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class CFGUtils {
    public static Type lowArrayDimension(Type type, int count) {
        if (type.getSort() == Type.ARRAY) {
            int dimension = getArrayDimension(type);
            if (dimension < count) {
                throw new IllegalArgumentException("Cannot low array dimension from to " + (dimension - count));
            }
            String lowedDescriptor = type.getDescriptor().substring(count);
            return Type.getType(lowedDescriptor);
        }
        throw new IllegalArgumentException("Internal error. Cannot low dimension of type " + type);
    }

    public static Type getArrayElementType(Type type) {
        if (type.getSort() == Type.ARRAY) {
            return lowArrayDimension(type, getArrayDimension(type));
        }
        throw new IllegalArgumentException();
    }

    private static int getArrayDimension(Type type) {
        return (int)type.getDescriptor().chars().filter(c -> c == '[').count();
    }
}
