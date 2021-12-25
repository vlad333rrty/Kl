package kalina.compiler.syntax.parser.data;

/**
 * @author vlad333rrty
 */
public class SimpleIndexGenerator implements IndexGenerator {
    private int index;

    public SimpleIndexGenerator(int start) {
        this.index = start;
    }

    @Override
    public int getNewIndex() {
        return index++;
    }
}
