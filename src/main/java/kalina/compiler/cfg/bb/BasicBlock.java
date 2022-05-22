package kalina.compiler.cfg.bb;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import kalina.compiler.instructions.Instruction;
import kalina.compiler.utils.PrintUtils;

/**
 * @author vlad333rrty
 */
public class BasicBlock {
    private final int id;
    private List<Instruction> instructions;

   private final PhiFunctionManager phiFunctionManager = new PhiFunctionManager();

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

    public void addPhiFunForVar(String varName) {
        phiFunctionManager.addPhiFun(varName);
    }

    public Optional<PhiFunction> getPhiFunForVar(String varName) {
        return Optional.ofNullable(phiFunctionManager.getForVar(varName));
    }

    public Map<String, PhiFunction> getVarNameToPhiFun() {
        return phiFunctionManager.getVarNameToPhiFun();
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (var phi : getVarNameToPhiFun().entrySet()) {
            builder.append(phi.getKey()).append(" = ").append(phi.getValue()).append("\n");
        }
        return PrintUtils.listToString(instructions) + "\n"
                + builder;
    }
}
