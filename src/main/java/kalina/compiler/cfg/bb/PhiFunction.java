package kalina.compiler.cfg.bb;

import java.util.List;

import kalina.compiler.cfg.data.SSAVariableInfo;

/**
 * @author vlad333rrty
 */
public class PhiFunction {
    private final List<SSAVariableInfo> arguments;

    public PhiFunction(List<SSAVariableInfo> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "φ(" + arguments.toString() + ")";
    }
}
