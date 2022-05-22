package kalina.compiler.cfg.data;

/**
 * @author vlad333rrty
 */
public class SSAVariableInfo {
    private int cfgIndex = 0;
    private final String name;

    public SSAVariableInfo(String name) {
        this.name = name;
    }

    public int getCfgIndex() {
        return cfgIndex;
    }

    public void setCfgIndex(int cfgIndex) {
        this.cfgIndex = cfgIndex;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + "_" + cfgIndex;
    }
}
