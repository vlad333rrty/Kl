package kalina.compiler.syntax.parser.data;

/**
 * @author vlad333rrty
 */
public class RuntimeConstantPool {
    private final String className;
    private final IFunctionTable functionTable;

    public RuntimeConstantPool(String className, IFunctionTable functionTable) {
        this.className = className;
        this.functionTable = functionTable;
    }

    public String getClassName() {
        return className;
    }

    public IFunctionTable getFunctionTable() {
        return functionTable;
    }
}
