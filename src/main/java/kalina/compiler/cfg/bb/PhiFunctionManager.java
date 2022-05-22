package kalina.compiler.cfg.bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import kalina.compiler.cfg.data.SSAVariableInfo;

/**
 * @author vlad333rrty
 */
public class PhiFunctionManager {
    private final Map<String, PhiFunction> varToPhiFun = new HashMap<>();

    public void addPhiFun(String varName) {
        varToPhiFun.put(
                varName,
                new PhiFunction(List.of(new SSAVariableInfo(varName), new SSAVariableInfo(varName))));
    }

    @Nullable
    public PhiFunction getForVar(String varName) {
        return varToPhiFun.get(varName);
    }

    public Map<String, PhiFunction> getVarNameToPhiFun() {
        return varToPhiFun;
    }
}
