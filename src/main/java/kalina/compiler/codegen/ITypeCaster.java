package kalina.compiler.codegen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public interface ITypeCaster {
    void cast(Type from, Type to, MethodVisitor mv) throws CodeGenException;
}
