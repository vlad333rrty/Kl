package kalina.compiler.cfg.exceptions;

/**
 * @author vlad333rrty
 */
public class CFGConversionException extends Exception {
    public CFGConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CFGConversionException(String message) {
        super(message);
    }
}
