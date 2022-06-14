package kalina.compiler.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public interface IExpressionCodeGen {
    void createVarDecl(MethodVisitor mv, String name, String descriptor, String signature, Label start, Label end, int index);

    void loadVariable(MethodVisitor mv, int opcode, int index);

    void putValueOnStack(MethodVisitor mv, Object value);

    void cast(Type from, Type to, MethodVisitor mv) throws CodeGenException;
}
