package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.expressions.CondExpression;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class CondInstruction extends Instruction {
    private final CondExpression condExpression;

    public CondInstruction(CondExpression condExpression) {
        this.condExpression = condExpression;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) {
        if (mv.isPresent()) {
            condExpression.translateToBytecode(mv.get());
        } else {
            throw new IllegalArgumentException();
        }
    }
}
