package kalina.compiler.instructions.v2.br;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class DoBlockBeginInstruction extends Instruction {
    private final Label blockBegin;

    public DoBlockBeginInstruction(Label blockBegin) {
        this.blockBegin = blockBegin;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            mv.get().visitLabel(blockBegin);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "do body start";
    }
}
