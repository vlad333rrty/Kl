package kalina.compiler.expressions.v2.array;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public interface AbstractArrayExpression {
    Logger logger = LogManager.getLogger(AbstractArrayExpression.class);

    default void translateElementsAccess(MethodVisitor mv, List<Integer> indices) {
        if (indices.isEmpty()) {
            logger.error("Array indexation error: empty indices array");
            return;
        }
        for (int j = 0; j < indices.size() - 1; j++) {
            int i = indices.get(j);
            visitIndexInstruction(mv, i);
            mv.visitInsn(Opcodes.AALOAD);
        }
        visitIndexInstruction(mv, indices.get(indices.size() - 1));
    }

    default void visitIndexInstruction(MethodVisitor mv, int index) {
        if (index <= 5) {
            mv.visitInsn(Opcodes.ICONST_0 + index);
        } else if (index <= 127) {
            mv.visitIntInsn(Opcodes.BIPUSH, index);
        } else if (index <= 32767) {
            mv.visitIntInsn(Opcodes.SIPUSH, index);
        } else {
            mv.visitLdcInsn(index);
        }
    }
}
