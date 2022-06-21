package kalina.compiler.cfg.converter.items;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public abstract class UnknownOwnerExpressionConverterBase {
    protected static void validateOwnerType(Type ownerType) {
        if (ownerType.getSort() == Type.VOID) {
            throw new IllegalArgumentException("Cannot call property of void");
        }
    }
}
