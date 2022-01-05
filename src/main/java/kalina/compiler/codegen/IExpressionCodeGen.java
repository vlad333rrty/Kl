package kalina.compiler.codegen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public interface IExpressionCodeGen {
    void add(MethodVisitor mv, int index1, int index2);

    void sub(MethodVisitor mv, int index1, int index2);

    void mul(MethodVisitor mv, int index1, int index2);

    void div(MethodVisitor mv, int index1, int index2);

    /**
     * opcode may be ASTORE, ISTORE, FSTORE etc
     */
    void storeValue(MethodVisitor mv, Object value, int opcode, int index);

    void createVarDecl(MethodVisitor mv, String name, String descriptor, String signature, Label start, Label end, int index);

    void createIfStmt(MethodVisitor mv, Label start, Label end); // todo

    void createForLoop(MethodVisitor mv, Label start, Label end); // todo

    MethodVisitor createMethod(ClassWriter cw, int access, String name, String descriptor, String signature, String[] exceptions);

    void loadVariable(MethodVisitor mv, int opcode, int index);

    void putValueOnStack(MethodVisitor mv, Object value);

    void createField(ClassWriter cw, String name, String descriptor, String signature, Object value);

    void cast(Type from, Type to, MethodVisitor mv) throws CodeGenException;
}
