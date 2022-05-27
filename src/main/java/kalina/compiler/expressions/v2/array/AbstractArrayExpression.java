package kalina.compiler.expressions.v2.array;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public interface AbstractArrayExpression {
    Logger logger = LogManager.getLogger(AbstractArrayExpression.class);

    default void translateElementsAccess(MethodVisitor mv, List<Expression> indices) throws CodeGenException {
        if (indices.isEmpty()) {
            logger.error("Array indexation error: empty indices array");
            return;
        }
        for (int i = 0; i < indices.size() - 1; i++) {
            indices.get(i).translateToBytecode(mv);
            mv.visitInsn(Opcodes.AALOAD);
        }
        indices.get(indices.size() - 1).translateToBytecode(mv);
    }
}
