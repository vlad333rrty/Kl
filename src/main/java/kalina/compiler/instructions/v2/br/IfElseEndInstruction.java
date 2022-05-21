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
public class IfElseEndInstruction extends Instruction {
    private final Label end;

    public IfElseEndInstruction(Label end) {
        this.end = end;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            mv.get().visitJumpInsn(Opcodes.GOTO, end);
            mv.get().visitLabel(end);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "else end";
    }
}
