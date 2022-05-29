package kalina.compiler.expressions.v2.array;

import java.util.List;

import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.WithSubstitutableExpressions;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FieldArrayGetElementExpression extends Expression implements
        AbstractArrayExpression,
        WithSubstitutableExpressions<Expression>
{
    private final OxmaFieldInfo fieldInfo;
    private final List<Expression> indices;
    private final Type loweredType;

    public FieldArrayGetElementExpression(
            OxmaFieldInfo fieldInfo,
            List<Expression> indices,
            Type loweredType)
    {
        this.fieldInfo = fieldInfo;
        this.indices = indices;
        this.loweredType = loweredType;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        int opcode = fieldInfo.isStatic() ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        if (!fieldInfo.isStatic()) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }
        mv.visitFieldInsn(opcode, fieldInfo.ownerClassName(), fieldInfo.fieldName(), fieldInfo.type().getDescriptor());
        translateElementsAccess(mv, indices);
    }

    @Override
    public Type getType() {
        return loweredType;
    }

    @Override
    public Expression substituteExpressions(List<Expression> expressions) {
        assert expressions.size() == indices.size();
        return new FieldArrayGetElementExpression(
                fieldInfo,
                expressions,
                loweredType
        );
    }

    public List<Expression> getIndices() {
        return indices;
    }

    @Override
    public String toString() {
        return fieldInfo.fieldName() + "[" + PrintUtils.listToString(indices) + "]";
    }
}
