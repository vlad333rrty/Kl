package kalina.compiler.bb;

import java.util.List;

import javax.annotation.Nullable;

import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public class RootBasicBlock extends AbstractBasicBlock {
    private final List<ClassBasicBlock> classBasicBlocks;

    public RootBasicBlock(List<ClassBasicBlock> classBasicBlocks) {
        this.classBasicBlocks = classBasicBlocks;
    }

    public List<ClassBasicBlock> getClassBasicBlocks() {
        return classBasicBlocks;
    }

    @Override
    @Nullable
    public Instruction getInstruction() {
        return null;
    }
}
