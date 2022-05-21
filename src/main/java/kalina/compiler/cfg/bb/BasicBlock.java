package kalina.compiler.cfg.bb;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public class BasicBlock {
    private final int id;
    private final List<Instruction> instructions;

    public BasicBlock(int id, List<Instruction> instructions) {
        this.id = id;
        this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return instructions.stream().map(Objects::toString).collect(Collectors.joining(", "));
    }
}
