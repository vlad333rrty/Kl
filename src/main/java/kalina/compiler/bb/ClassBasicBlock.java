package kalina.compiler.bb;

import kalina.compiler.instructions.DefaultConstructorInstruction;
import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public class ClassBasicBlock extends AbstractBasicBlock {
    private final DefaultConstructorInstruction instruction;

    public ClassBasicBlock(DefaultConstructorInstruction instruction) {
        this.instruction = instruction;
    }

    public String getClassName() {
        return instruction.getName();
    }

    @Override
    public Instruction getInstruction() {
        return instruction;
    }
}
