package kalina.compiler.cfg.data;

import java.util.List;

import kalina.compiler.expressions.Expression;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class AssignArrayVariableInfo {
    private List<Expression> indices;
    private final Type elementType;
    private final Type loweredType;

    public AssignArrayVariableInfo(List<Expression> indices, Type elementType, Type loweredType) {
        this.indices = indices;
        this.elementType = elementType;
        this.loweredType = loweredType;
    }

    public List<Expression> getIndices() {
        return indices;
    }

    public Type getElementType() {
        return elementType;
    }

    public Type getLoweredType() {
        return loweredType;
    }

    public void setIndices(List<Expression> indices) {
        this.indices = indices;
    }
}
