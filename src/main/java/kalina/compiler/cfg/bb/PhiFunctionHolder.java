package kalina.compiler.cfg.bb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

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

    @Nullable
    public PhiFunction getForVar(String varName) {
        SSAVariableInfo variableInfo = nameToSSAInfo.get(varName);
        return varInfoToPhiFun.get(variableInfo);
    }

    public Map<SSAVariableInfo, PhiFunction> getVarNameToPhiFun() {
        return varInfoToPhiFun;
    }

    public void updateArgument(String varName, int cfgIndex, int parentBlockId) {
        int argPos = blockIdToPhiArgPos.getOrDefault(parentBlockId, -1);
        if (argPos < 0) {
            return;
        }
        Objects.requireNonNull(getForVar(varName)).updateArgument(cfgIndex, argPos);
    }

    public void putBlockIdToPhiFunArg(int blockId, int argPos) {
        blockIdToPhiArgPos.put(blockId, argPos);
    }
}
