package kalina.compiler.cfg.bb;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.instructions.Instruction;
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


    public void addPhiFunForVar(String varName, int dimension) {
        phiFunctionHolder.addPhiFun(varName, dimension);
    }

    public Optional<PhiFunction> getPhiFunForVar(String varName) {
        return Optional.ofNullable(phiFunctionHolder.getForVar(varName));
    }

    public Map<SSAVariableInfo, PhiFunction> getVarInfoToPhiFun() {
        return phiFunctionHolder.getVarNameToPhiFun();
    }

    public void updatePhiFunArg(String varName, int cfgIndex, int parentBlockId) {
        phiFunctionHolder.updateArgument(varName, cfgIndex, parentBlockId);
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void setNewId(int newId) {
        this.newId = Optional.of(newId);
    }

    public PhiFunctionHolder getPhiFunctionHolder() {
        return phiFunctionHolder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BLOCK_ID: ").append(getId()).append("\n");
        for (var phi : getVarInfoToPhiFun().entrySet()) {
            builder.append(phi.getKey()).append(" = ").append(phi.getValue()).append("\n");
        }
        return builder.append("\n").append(PrintUtils.listToString(instructions)).toString();
    }
}
