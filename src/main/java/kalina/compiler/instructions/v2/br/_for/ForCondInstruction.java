package kalina.compiler.instructions.v2.br._for;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithCondition;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class ForCondInstruction extends Instruction implements WithCondition {
    private final CondExpression condition;
    private final Label start;

    public ForCondInstruction(CondExpression condition, Label start) {
        this.condition = condition;
        this.start = start;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            methodVisitor.visitLabel(start);
            condition.translateToBytecode(methodVisitor);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public CondExpression getCondExpression() {
        return condition;
    }

    @Override
    public Instruction substituteCondExpression(CondExpression expression) {
        return new ForCondInstruction(expression, start);
    }

    @Override
    public String toString() {
        return condition.toString();
    }
}
