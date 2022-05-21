package kalina.compiler.instructions.v2.br;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class IfCondInstruction extends Instruction {
    private final CondExpression condition;
    private final Label label;

    public IfCondInstruction(CondExpression condition) {
        this.condition = condition;
        this.label = condition.getLabel();
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            condition.translateToBytecode(methodVisitor);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return condition.toString();
    }
}
