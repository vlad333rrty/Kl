package kalina.compiler.cfg.bb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import kalina.compiler.cfg.data.SSAVariableInfo;

/**
 * @author vlad333rrty
 */
public class PhiFunctionHolder {
    private final Map<SSAVariableInfo, PhiFunction> varInfoToPhiFun = new HashMap<>();
    private final Map<String, SSAVariableInfo> nameToSSAInfo = new HashMap<>();
    private final Map<Integer, Integer> blockIdToPhiArgPos = new HashMap<>();

    public void addPhiFun(String varName, int dimension) {
        SSAVariableInfo variableInfo = new SSAVariableInfo(varName);
        nameToSSAInfo.put(varName, variableInfo);
        List<SSAVariableInfo> args = new ArrayList<>();
        IntStream.range(0, dimension).forEach(i -> args.add(new SSAVariableInfo(varName)));
        varInfoToPhiFun.put(variableInfo, new PhiFunction(args));
    }

    public Optional<PhiFunction> getForVar(String varName) {
        SSAVariableInfo variableInfo = nameToSSAInfo.get(varName);
        return Optional.ofNullable(varInfoToPhiFun.get(variableInfo));
    }

    public Map<SSAVariableInfo, PhiFunction> getVarInfoToPhiFun() {
        return varInfoToPhiFun;
    }

    public void updatePhiFunArgument(String varName, int cfgIndex, int parentBlockId) {
        int argPos = blockIdToPhiArgPos.getOrDefault(parentBlockId, -1);
        if (argPos < 0) {
            return;
        }
        getForVar(varName).orElseThrow().updateArgument(cfgIndex, argPos);
    }

    public void putBlockIdToPhiFunArg(int blockId, int argPos) {
        blockIdToPhiArgPos.put(blockId, argPos);
    }
}
