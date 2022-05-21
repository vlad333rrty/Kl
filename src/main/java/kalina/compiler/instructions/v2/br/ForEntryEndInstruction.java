package kalina.compiler.instructions.v2.br;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class ForEntryEndInstruction extends Instruction {
    private final Label start;
    private final Label end;

    public ForEntryEndInstruction(Label start, Label end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            methodVisitor.visitJumpInsn(Opcodes.GOTO, start);
            methodVisitor.visitLabel(end);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "for body end";
    }
}
