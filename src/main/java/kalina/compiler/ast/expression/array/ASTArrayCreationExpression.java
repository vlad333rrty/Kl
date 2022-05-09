package kalina.compiler.ast.expression.array;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ASTArrayCreationExpression implements ASTExpression {
    private final List<Integer> capacities;
    private final Type arrayType;
    private final Type elementType;

    public ASTArrayCreationExpression(List<Integer> capacities, Type arrayType, Type elementType) {
        this.capacities = capacities;
        this.arrayType = arrayType;
        this.elementType = elementType;
    }

    public List<Integer> getCapacities() {
        return capacities;
    }

    public Type getArrayType() {
        return arrayType;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public String toString() {
        return arrayType.toString() + capacities;
    }
}
