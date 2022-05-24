package kalina.compiler.instructions.v2.br;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithCondition;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class DoBlockEndInstruction extends Instruction implements WithCondition {
    private final CondExpression condition;

    public DoBlockEndInstruction(CondExpression condition) {
        this.condition = condition;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            condition.translateToBytecode(mv.get());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "while " + condition.toString();
    }

    @Override
    public CondExpression getCondExpression() {
        return condition;
    }
}
