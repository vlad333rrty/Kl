package kalina.compiler.instructions.v2.fake;

import kalina.compiler.expressions.ValueExpression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FakeValueExpression extends ValueExpression implements PhiArgument {
    private static final Type DEFAULT_TYPE = Type.INT_TYPE;

    public FakeValueExpression(Object value) {
        super(value, DEFAULT_TYPE);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) {
        // do nothing
    }
}
