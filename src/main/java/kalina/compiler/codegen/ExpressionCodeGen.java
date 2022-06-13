package kalina.compiler.codegen;

import kalina.compiler.codegen.typeCast.ITypeCaster;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ExpressionCodeGen implements IExpressionCodeGen {
    private final ITypeCaster typeCaster;

    public ExpressionCodeGen(ITypeCaster typeCaster) {
        this.typeCaster = typeCaster;
    }

    @Override
    public void createVarDecl(MethodVisitor mv, String name, String descriptor, String signature, Label start,
                              Label end, int index) {
        mv.visitLocalVariable(name, descriptor, signature, start, end, index);
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
    public void cast(Type from, Type to, MethodVisitor mv) throws CodeGenException {
        typeCaster.cast(from, to, mv);
    }
}
