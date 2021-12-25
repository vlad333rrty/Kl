package kalina.compiler.syntax.grammar;

import java.util.Objects;

/**
 * @author vlad333rrty
 */
public abstract class GObject {
    private final String value;

    protected GObject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
