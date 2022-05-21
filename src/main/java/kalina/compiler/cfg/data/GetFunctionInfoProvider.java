package kalina.compiler.cfg.data;

import java.util.Map;
import java.util.Optional;

import kalina.compiler.cfg.traverse.OxmaFunctionInfoProvider;

/**
 * @author vlad333rrty
 */
public class GetFunctionInfoProvider {
    private final Map<String, OxmaFunctionInfoProvider> classNameToFunctionTable;

    public GetFunctionInfoProvider(Map<String, OxmaFunctionInfoProvider> classNameToFunctionTable) {
        this.classNameToFunctionTable = classNameToFunctionTable;
    }

    public Optional<OxmaFunctionInfoProvider> getFunctionTable(String className) {
        return Optional.ofNullable(classNameToFunctionTable.get(className));
    }
}
