package kalina.compiler.bb;

import java.util.List;

/**
 * @author vlad333rrty
 */
public class RootBasicBlock {
    private final List<ClassBasicBlock> classBasicBlocks;

    public RootBasicBlock(List<ClassBasicBlock> classBasicBlocks) {
        this.classBasicBlocks = classBasicBlocks;
    }

    public List<ClassBasicBlock> getClassBasicBlocks() {
        return classBasicBlocks;
    }
}
