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
public class IfThenEndInstruction extends Instruction {
    private final Label elseLabel;
    private final Label end;
    private final boolean elseIsPresent;

    public IfThenEndInstruction(Label elseLabel, Label end, boolean elseIsPresent) {
        this.elseLabel = elseLabel;
        this.end = end;
        this.elseIsPresent = elseIsPresent;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            if (elseIsPresent) {
                mv.get().visitJumpInsn(Opcodes.GOTO, end);
            }
            mv.get().visitLabel(elseLabel);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "then end";
    }
}
