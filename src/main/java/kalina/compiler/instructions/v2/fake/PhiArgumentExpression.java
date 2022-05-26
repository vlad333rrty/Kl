package kalina.compiler.instructions.v2.fake;

import kalina.compiler.expressions.VariableExpression;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class PhiArgumentExpression extends VariableExpression {
    private static final int DEFAULT_INDEX = 0;

    public PhiArgumentExpression(String name, int cfgIndex) {
        super(DEFAULT_INDEX, Type.INT_TYPE, name, cfgIndex);
    }
}
