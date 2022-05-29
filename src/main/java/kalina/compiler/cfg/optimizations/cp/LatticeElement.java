package kalina.compiler.cfg.optimizations.cp;

/**
 * @author vlad333rrty
 */
class LatticeElement {
    public Type type;
    public Object value;

    public LatticeElement() {
        this.type = Type.CONFLICT;
    }

    public LatticeElement(Type type, Object value) {
        this.type = type;
        this.value = value;
    }


    public enum Type {
        CONFLICT, CONST, NO_ASSIGNMENT
    }
}
