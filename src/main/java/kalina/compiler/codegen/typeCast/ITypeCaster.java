package kalina.compiler.codegen.typeCast;

import kalina.compiler.codegen.CodeGenException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public interface ITypeCaster {
    void cast(Type from, Type to, MethodVisitor mv) throws CodeGenException;
}
