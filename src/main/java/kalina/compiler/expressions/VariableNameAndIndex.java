package kalina.compiler.expressions;

import kalina.compiler.cfg.data.SSAVariableInfo;

/**
 * @author vlad333rrty
 */
public class VariableNameAndIndex {
    private final String name;
    private final int index;
    private final SSAVariableInfo ssaVariableInfo;

    public VariableNameAndIndex(String name, int index) {
        this.name = name;
        this.index = index;
        this.ssaVariableInfo = new SSAVariableInfo(name);
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public SSAVariableInfo getSsaVariableInfo() {
        return ssaVariableInfo;
    }

    @Override
    public String toString() {
        return ssaVariableInfo.toString();
    }
}
