package kalina.compiler.cfg.traverse;

import java.util.Map;
import java.util.Optional;

import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;

/**
 * @author vlad333rrty
 */
public class FunctionTableProvider {
    private final Map<String, OxmaFunctionTable> classNameToFunctionTable;

    public FunctionTableProvider(Map<String, OxmaFunctionTable> classNameToFunctionTable) {
        this.classNameToFunctionTable = classNameToFunctionTable;
    }

    public Optional<OxmaFunctionTable> getFunctionTable(String className) {
        return Optional.ofNullable(classNameToFunctionTable.get(className));
    }
}
