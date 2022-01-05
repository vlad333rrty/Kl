package kalina.compiler.expressions;

import java.util.List;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class LHS {
    private final List<VariableNameAndIndex> vars;
    private final Type type;

    public LHS(List<VariableNameAndIndex> vars, Type type) {
        this.vars = vars;
        this.type = type;
    }

    public List<VariableNameAndIndex> getVars() {
        return vars;
    }

    public Type getType() {
        return type;
    }

    public int size() {
        return vars.size();
    }
}
