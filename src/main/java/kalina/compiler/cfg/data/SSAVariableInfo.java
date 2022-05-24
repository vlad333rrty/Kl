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

    public SSAVariableInfo(String name, int cfgIndex) {
        this.cfgIndex = cfgIndex;
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

    public String getIR() {
        return name + "_" + cfgIndex;
    }

    @Override
    public String toString() {
        return getName() + "_" + cfgIndex;
    }
}
