package kalina.compiler.bb;

import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public class BasicBlock extends AbstractBasicBlock {
    private final Instruction instruction;

    public BasicBlock(Instruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public Instruction getInstruction() {
        return instruction;
    }
}
