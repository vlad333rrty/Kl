package kalina.compiler.expressions;

/**
 * @author vlad333rrty
 */
public class VariableNameAndIndex {
    private final String name;
    private final int index;

    public VariableNameAndIndex(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}
