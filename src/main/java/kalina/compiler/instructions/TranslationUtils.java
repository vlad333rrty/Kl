package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.codegen.CodeGenException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public final class TranslationUtils {
    public static void translateBlock(Optional<AbstractBasicBlock> bb, Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        Optional<AbstractBasicBlock> current = bb;
        while (current.isPresent()) {
            current.get().getInstruction().translateToBytecode(mv, cw);
            current = current.get().getNext();
        }
    }
}
