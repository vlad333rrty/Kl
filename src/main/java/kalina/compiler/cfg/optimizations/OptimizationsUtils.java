package kalina.compiler.cfg.optimizations;

import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.instructions.Instruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class OptimizationsUtils {
    private static final Logger logger = LogManager.getLogger(OptimizationsUtils.class);

    public static Instruction getBBInstruction(BasicBlock basicBlock, DuUdNet.InstructionCoordinates coordinates) {
        if (coordinates.instructionIndex() < basicBlock.getInstructions().size()) {
            return basicBlock.getInstructions().get(coordinates.instructionIndex());
        } else {
            int offset = basicBlock.getPhiFunInstructions().size();
            if (coordinates.instructionIndex() < offset) {
                return basicBlock.getPhiFunInstructions().get(coordinates.instructionIndex());
            } else {
                return basicBlock.getInstructions().get(coordinates.instructionIndex() - offset);
            }
        }
    }
}
