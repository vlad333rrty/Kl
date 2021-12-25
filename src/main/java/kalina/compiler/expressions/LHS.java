package kalina.compiler.expressions;

import java.util.List;

/**
 * @author vlad333rrty
 */
public class LHS {
    private final List<VariableInfo> vars;

    public LHS(List<VariableInfo> vars) {
        this.vars = vars;
    }

    public List<VariableInfo> getVars() {
        return vars;
    }
}
