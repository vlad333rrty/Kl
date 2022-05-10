package kalina.compiler.syntax.parser.data;

import java.util.List;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArrayVariableInfo {
    private final List<Integer> capacities;
    private final Type elementType;

    public ArrayVariableInfo(List<Integer> capacities, Type elementType) {
        this.capacities = capacities;
        this.elementType = elementType;
    }

    public Type getElementType() {
        return elementType;
    }
}
