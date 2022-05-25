package kalina.compiler.instructions.v2.br;

import java.util.List;
import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithCondition;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class IfCondInstruction extends Instruction implements WithCondition {
    private final CondExpression condition;

    public IfCondInstruction(CondExpression condition) {
        this.condition = condition;
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
        return condition.getLabel();
    }

    @Override
    public String toString() {
        return condition.toString();
    }

    public CondExpression getCondition() {
        return condition;
    }

    @Override
    public CondExpression getCondExpression() {
        return getCondition();
    }

    @Override
    public List<Expression> getExpressions() {
        return List.of(condition);
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        return new IfCondInstruction((CondExpression) expressions.stream().findFirst().orElseThrow());
    }
}
