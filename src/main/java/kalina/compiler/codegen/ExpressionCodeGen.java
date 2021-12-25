package kalina.compiler.codegen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class ExpressionCodeGen implements IExpressionCodeGen {

    @Override
    public void add(MethodVisitor mv, int index1, int index2) {

    }

    @Override
    public void sub(MethodVisitor mv, int index1, int index2) {

    }

    @Override
    public void mul(MethodVisitor mv, int index1, int index2) {

    }

    @Override
    public void div(MethodVisitor mv, int index1, int index2) {

    }

    @Override
    public void storeValue(MethodVisitor mv, Object value, int opcode, int index) {
        mv.visitLdcInsn(value);
        mv.visitVarInsn(opcode, index);
    }

    @Override
    public void createVarDecl(MethodVisitor mv, String name, String descriptor, String signature, Label start,
                              Label end, int index) {
        mv.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public void createIfStmt(MethodVisitor mv, Label start, Label end) {

    }

    @Override
    public void createForLoop(MethodVisitor mv, Label start, Label end) {

    }

    @Override
    public MethodVisitor createMethod(ClassWriter cw, int access, String name, String descriptor, String signature, String[] exceptions) {
        return cw.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void loadVariable(MethodVisitor mv, int opcode, int index) {
        mv.visitVarInsn(opcode, index);
    }

    @Override
    public void putValueOnStack(MethodVisitor mv, Object value) {
        mv.visitLdcInsn(value);
    }

    @Override
    public void createField(ClassWriter cw, String name, String descriptor, String signature, Object value) {
        cw.visitField(Opcodes.ACC_PUBLIC, name, descriptor, signature, value);
    }
}
