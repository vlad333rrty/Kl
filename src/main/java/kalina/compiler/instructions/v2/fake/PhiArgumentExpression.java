package kalina.compiler.instructions.v2.fake;

import java.util.Optional;

import kalina.compiler.expressions.VariableExpression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class PhiArgumentExpression extends VariableExpression implements PhiArgument {
    private static final int DEFAULT_INDEX = 0;

    private Optional<FakeValueExpression> fakeValueExpression = Optional.empty();

    public PhiArgumentExpression(String name, int cfgIndex) {
        super(DEFAULT_INDEX, Type.INT_TYPE, name, cfgIndex);
    }

    public void setFakeValueExpression(FakeValueExpression fakeValueExpression) {
        this.fakeValueExpression = Optional.of(fakeValueExpression);
    }

    @Override
    public String toString() {
        if (fakeValueExpression.isPresent()) {
            return fakeValueExpression.get().toString();
        } else {
            return super.toString();
        }
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) {
        // do nothing
    }
}
