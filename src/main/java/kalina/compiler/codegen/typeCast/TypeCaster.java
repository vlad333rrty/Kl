package kalina.compiler.codegen.typeCast;

import kalina.compiler.codegen.CodeGenException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class TypeCaster implements ITypeCaster {
    @Override
    public void cast(Type from, Type to, MethodVisitor mv) throws CodeGenException {
        if (from.equals(to)) {
            return;
        }
        if (!TypeCastValidator.canCast(from, to)) {
            throw new CodeGenException("Cannot cast " + from.getClassName() + " to " + to.getClassName());
        }
        mv.visitInsn(TypeCastOpcodesMapper.getCastOpcode(from, to).orElseThrow());
    }
}
