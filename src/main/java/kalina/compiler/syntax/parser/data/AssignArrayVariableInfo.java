package kalina.compiler.syntax.parser.data;

import java.util.List;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class AssignArrayVariableInfo {
    private final List<Integer> indices;
    private final Type elementType;
    private final Type loweredType;

    public AssignArrayVariableInfo(List<Integer> indices, Type elementType, Type loweredType) {
        this.indices = indices;
        this.elementType = elementType;
        this.loweredType = loweredType;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public Type getElementType() {
        return elementType;
    }

    public Type getLoweredType() {
        return loweredType;
    }
}
