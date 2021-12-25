import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class Test {
    public static void main(String[] args) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null);
        MethodVisitor methodVisitor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0); //load the first local variable: this
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(1, 1); // this just triggers asm to compute proper stack size (because of ClassWriter.COMPUTE_MAXES)
        methodVisitor.visitEnd();

        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main", "([Ljava/lang/String;)V", null, null);

        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, new Label(), new Label(), 0);
        mv.visitLdcInsn(false);
        Label label = new Label();
        Label end = new Label();
        mv.visitJumpInsn(Opcodes.IFLT, label);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("TRUE");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);

        mv.visitJumpInsn(Opcodes.GOTO, end);

        mv.visitLabel(label);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("FALSE");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
        mv.visitJumpInsn(Opcodes.GOTO, end);

        mv.visitLabel(end);
        mv.visitInsn(Opcodes.RETURN);


        mv.visitMaxs(1, 1);
        mv.visitEnd();
        cw.visitEnd();

        writeToFile(cw.toByteArray(), "Test.class");
    }

    private static void writeToFile(byte[] bytes, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
