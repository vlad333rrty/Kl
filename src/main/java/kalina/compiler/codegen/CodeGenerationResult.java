package kalina.compiler.codegen;

/**
 * @author vlad333rrty
 */
public class CodeGenerationResult {
    private final byte[] byteCode;
    private final String className;

    public CodeGenerationResult(byte[] byteCode, String className) {
        this.byteCode = byteCode;
        this.className = className;
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    public String getClassName() {
        return className;
    }
}
