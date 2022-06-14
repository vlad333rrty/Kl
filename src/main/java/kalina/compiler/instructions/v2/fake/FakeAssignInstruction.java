package kalina.compiler.instructions.v2.fake;

import kalina.compiler.cfg.data.WithIR;

/**
 * @author vlad333rrty
 */
public class FakeAssignInstruction extends FakeInstruction implements PhiFunOrFakeAssignInstruction, WithIR {
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

    @Override
    public String getIR() {
        return lhsIR;
    }
}
