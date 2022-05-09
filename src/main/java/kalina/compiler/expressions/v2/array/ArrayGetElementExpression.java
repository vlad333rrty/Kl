package kalina.compiler.expressions.v2.array;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArrayGetElementExpression extends Expression implements AbstractArrayExpression {
    private final List<Integer> indices;
    private final Type elementType;
    private final Type loweredType;
    private final Type initialType;
    private final int index;

    public ArrayGetElementExpression(List<Integer> indices, Type elementType, Type loweredType, Type initialType, int index) {
        this.indices = indices;
        this.elementType = elementType;
        this.loweredType = loweredType;
        this.initialType = initialType;
        this.index = index;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        expressionCodeGen.loadVariable(mv, initialType.getOpcode(Opcodes.ILOAD), index);
        translateElementsAccess(mv, indices);
        mv.visitInsn(elementType.getOpcode(Opcodes.IALOAD));
    }

    @Override
    public Type getType() {
        return loweredType;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public Type getInitialType() {
        return initialType;
    }

    public int getIndex() {
        return index;
    }

    public Type getElementType() {
        return elementType;
    }
}
