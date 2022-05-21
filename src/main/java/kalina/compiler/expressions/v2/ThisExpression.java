package kalina.compiler.expressions.v2;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ThisExpression extends Expression {
    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
    }

    @Override
    public Type getType() {
        return Type.getType(Object.class); // todo consider smart type checking
    }
}
