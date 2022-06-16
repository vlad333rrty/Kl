package kalina.compiler.instructions;

import java.util.Optional;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class DefaultConstructorInstruction extends Instruction {
    private final String name;
    private final String superClassName;

    public DefaultConstructorInstruction(String name, String superClassName) {
        this.name = name;
        this.superClassName = superClassName;
    }

    public String getName() {
        return name;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) {
        if (cw.isPresent()) {
            ClassWriter classWriter = cw.get();
            classWriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, name, null, superClassName, null);
            MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0); //load the first local variable: this
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassName, "<init>", "()V", false);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(1000, 1000); // this just triggers asm to compute proper stack size (because of ClassWriter.COMPUTE_MAXES)
            methodVisitor.visitEnd();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
