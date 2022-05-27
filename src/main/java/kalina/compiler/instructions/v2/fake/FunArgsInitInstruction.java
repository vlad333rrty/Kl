package kalina.compiler.instructions.v2.fake;

import java.util.List;

import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.instructions.v2.WithLHS;

/**
 * @author vlad333rrty
 */
public class FunArgsInitInstruction extends FakeInstruction implements WithLHS {
    private final List<SSAVariableInfo> argInfos;

    public FunArgsInitInstruction(List<String> argNames) {
        this.argInfos = argNames.stream().map(SSAVariableInfo::new).toList();
    }

    @Override
    public String toString() {
        return "fun arguments: " + argInfos.stream().map(SSAVariableInfo::getIR).toList();
    }

    @Override
    public List<SSAVariableInfo> getVariableInfos() {
        return argInfos;
    }
}
