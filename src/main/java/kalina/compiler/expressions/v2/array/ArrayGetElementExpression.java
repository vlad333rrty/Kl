package kalina.compiler.expressions.v2.array;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArrayGetElementExpression extends Expression implements AbstractArrayExpression {
    private final List<Expression> indices;
    private final Type elementType;
    private final Type loweredType;
    private final Type initialType;
    private final Expression variableAccessExpression;

    public ArrayGetElementExpression(List<Expression> indices, Type elementType, Type loweredType, Type initialType, Expression variableAccessExpression) {
        this.indices = indices;
        this.elementType = elementType;
        this.loweredType = loweredType;
        this.initialType = initialType;
        this.variableAccessExpression = variableAccessExpression;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        variableAccessExpression.translateToBytecode(mv);
        translateElementsAccess(mv, indices);
        mv.visitInsn(elementType.getOpcode(Opcodes.IALOAD));
    }

    @Override
    public Type getType() {
        return loweredType;
    }

    public List<Expression> getIndices() {
        return indices;
    }

    public Type getInitialType() {
        return initialType;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public String toString() {
        return PrintUtils.listToString(indices);
    }
}
