package kalina.compiler.syntax.grammar;

/**
 * @author vlad333rrty
 */
public class NonTerminal extends GObject {
    public NonTerminal(String value) {
        super(value);
    }

    @Override
    public String toString() {
        return "NonTerminal: " + getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NonTerminal) {
            return getValue().equals(((NonTerminal) obj).getValue());
        }
        return false;
    }
}
