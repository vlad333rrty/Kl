package kalina.compiler.expressions.v2.field;

import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FieldAccessExpression extends Expression {
    protected final Type type;
    protected final boolean isStatic;
    protected final String ownerClassName;
    protected final String fieldName;

    public FieldAccessExpression(Type type, boolean isStatic, String ownerClassName, String fieldName) {
        this.type = type;
        this.isStatic = isStatic;
        this.ownerClassName = ownerClassName;
        this.fieldName = fieldName;
    }

    public static FieldAccessExpression fromFieldInfoAndName(OxmaFieldInfo fieldInfo, String name) {
        return new FieldAccessExpression(
                fieldInfo.type(), fieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC),
                fieldInfo.ownerClassName(), name
        );
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        int opcode = isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        if (!isStatic) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }
        mv.visitFieldInsn(opcode, ownerClassName, fieldName, type.getDescriptor());
    }

    @Override
    public Type getType() {
        return type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
