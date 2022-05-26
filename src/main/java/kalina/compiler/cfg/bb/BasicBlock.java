package kalina.compiler.cfg.bb;

import java.util.List;
import java.util.Optional;

import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.fake.PhiFunInstruction;
import kalina.compiler.utils.PrintUtils;

/**
 * @author vlad333rrty
 */
public class BasicBlock {
    private final int id;
    private List<Instruction> instructions;

    // после создания cfg нумеруем ноды в порядке обхода, новый индекс пишется сюда. todo пересоздавать?
    private Optional<Integer> newId = Optional.empty();

    private final PhiFunctionHolder phiFunctionHolder = new PhiFunctionHolder();
    private List<PhiFunInstruction> phiFunInstructions = List.of();

    public BasicBlock(int id, List<Instruction> instructions) {
        this.id = id;
        this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public int getId() {
        return newId.orElse(id);
    }

    public int getOldId() {
        return id;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void setNewId(int newId) {
        this.newId = Optional.of(newId);
    }

    public void setPhiFunInstructions(List<PhiFunInstruction> phiFunInstructions) {
        this.phiFunInstructions = phiFunInstructions;
    }

    public List<PhiFunInstruction> getPhiFunInstructions() {
        return phiFunInstructions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BLOCK_ID: ").append(getId()).append("\n");
        for (var phi : phiFunInstructions) {
            builder.append(phi.toString()).append("\n");
        }
        return builder.append("\n").append(PrintUtils.listToString(instructions)).toString();
    }
}
