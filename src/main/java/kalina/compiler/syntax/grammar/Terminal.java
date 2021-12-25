package kalina.compiler.syntax.grammar;

/**
 * @author vlad333rrty
 */
public class Terminal extends GObject{

    public Terminal(String value) {
        super(value);
    }

    @Override
    public String toString() {
        return "Terminal: " + getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Terminal){
            return getValue().equals(((Terminal) obj).getValue());
        }
        return false;
    }
}
