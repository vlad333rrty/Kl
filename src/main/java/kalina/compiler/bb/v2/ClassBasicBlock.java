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
    private final List<FieldBasicBlock> fieldBasicBlocks;

    public ClassBasicBlock(DefaultConstructorInstruction instruction, List<FunBasicBlock> entry, List<FieldBasicBlock> fieldBasicBlocks) {
        this.instruction = instruction;
        this.entry = entry;
        this.fieldBasicBlocks = fieldBasicBlocks;
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

    public List<FieldBasicBlock> getFieldBasicBlocks() {
        return fieldBasicBlocks;
    }
}
