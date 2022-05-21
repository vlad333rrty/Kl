package kalina.compiler.instructions.v2.br;

import java.util.Objects;
import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class ForHeaderInstruction extends Instruction {
    private final Optional<Instruction> declarations;
    private final Optional<CondExpression> condition;
    private final Label start;

    public ForHeaderInstruction(Optional<Instruction> declarations, Optional<CondExpression> condition, Label start) {
        this.declarations = declarations;
        this.condition = condition;
        this.start = start;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            if (declarations.isPresent()) {
                Instruction instruction = declarations.get();
                instruction.translateToBytecode(mv, cw);
            }

            if (condition.isPresent()) {
                CondExpression condExpression = condition.get();
                methodVisitor.visitLabel(start);
                condExpression.translateToBytecode(methodVisitor);
            }

            methodVisitor.visitJumpInsn(Opcodes.GOTO, start);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return declarations.map(Objects::toString).orElse("") + "\n" + condition.map(CondExpression::toString).orElse("");
    }
}
