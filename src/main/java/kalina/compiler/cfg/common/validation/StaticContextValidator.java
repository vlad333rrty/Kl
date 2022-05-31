package kalina.compiler.cfg.common.validation;

/**
 * @author vlad333rrty
 */
public final class StaticContextValidator {
    public static void validateStaticContext(boolean isStatic, String memberName) {
        if (!isStatic) {
            throw new IllegalArgumentException("Cannot access non static member from the static context: " + memberName);
        }
    }
}
