package kalina.compiler.bb.v2;

import java.util.List;

import kalina.compiler.instructions.DefaultConstructorInstruction;
import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public class ClassBasicBlock {
    private final DefaultConstructorInstruction instruction;
    private final List<FunBasicBlock> entry;

    public ClassBasicBlock(DefaultConstructorInstruction instruction, List<FunBasicBlock> entry) {
        this.instruction = instruction;
        this.entry = entry;
    }

    public List<FunBasicBlock> getEntry() {
        return entry;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public String getClassName() {
        return instruction.getName();
    }
}
