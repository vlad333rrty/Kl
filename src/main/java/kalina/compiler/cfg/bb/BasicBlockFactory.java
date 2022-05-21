package kalina.compiler.cfg.bb;

import java.util.List;

import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public final class BasicBlockFactory {
    private static int index = 0;

    public static BasicBlock createBasicBlock(List<Instruction> instructions) {
        return new BasicBlock(index++, instructions);
    }
}
