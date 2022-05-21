package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * Just wraps an expression
 *
 * @author vlad333rrty
 */
public class SimpleInstruction extends Instruction {
    private final Expression expression;

    public SimpleInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            expression.translateToBytecode(mv.get());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return expression.toString();
    }
}
