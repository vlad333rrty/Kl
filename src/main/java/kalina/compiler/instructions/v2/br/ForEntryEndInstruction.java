package kalina.compiler.instructions.v2.br;

import java.util.List;
import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithExpressions;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class ForEntryEndInstruction extends Instruction implements WithExpressions {
    private final Label start;
    private final Label end;
    private final Optional<Instruction> action;

    public ForEntryEndInstruction(Label start, Label end, Optional<Instruction> action) {
        this.start = start;
        this.end = end;
        this.action = action;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            if (action.isPresent()) {
                action.get().translateToBytecode(mv, cw);
            }
            methodVisitor.visitJumpInsn(Opcodes.GOTO, start);
            methodVisitor.visitLabel(end);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "action: " + (action.isPresent() ? action.get().toString() : "none") + " - for body end";
    }

    @Override
    public List<Expression> getExpressions() {
        if (action.isEmpty()) {
            return List.of();
        }
        if (action.get() instanceof WithExpressions) {
            return ((WithExpressions) action.get()).getExpressions();
        }
        return List.of();
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        throw new UnsupportedOperationException();
    }
}
