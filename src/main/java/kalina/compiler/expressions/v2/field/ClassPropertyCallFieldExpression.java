package kalina.compiler.expressions.v2.field;

import kalina.compiler.codegen.CodeGenException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ClassPropertyCallFieldExpression extends FieldAccessExpression {
    public ClassPropertyCallFieldExpression(Type type, String ownerClassName, String fieldName) {
        super(type, false, ownerClassName, fieldName);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        mv.visitFieldInsn(Opcodes.GETFIELD, ownerClassName, fieldName, type.getDescriptor());
    }
}
