package kalina.compiler.instructions.v2.fake;

/**
 * @author vlad333rrty
 */
public class FakeAssignInstruction extends FakeInstruction {
    private final String lhsIR;
    private final Object value;

    public FakeAssignInstruction(String lhsIR, Object value) {
        this.lhsIR = lhsIR;
        this.value = value;
    }

    public String getLhsIR() {
        return lhsIR;
    }

    public Object getValue() {
        return value;
    }
}
