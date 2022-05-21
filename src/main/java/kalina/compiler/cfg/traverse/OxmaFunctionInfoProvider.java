package kalina.compiler.cfg.traverse;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.data.OxmaFunctionInfo;
import kalina.compiler.cfg.data.OxmaFunctionTable;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class OxmaFunctionInfoProvider {
    private final OxmaFunctionTable functionTable;
    private final OxmaFunctionInfoProvider parent;

    public OxmaFunctionInfoProvider(OxmaFunctionTable functionTable) {
        this.functionTable = functionTable;
        this.parent = null;
    }

    public OxmaFunctionInfoProvider(OxmaFunctionTable functionTable, OxmaFunctionInfoProvider parent) {
        this.functionTable = functionTable;
        this.parent = parent;
    }

    public OxmaFunctionInfoProvider withParent(OxmaFunctionInfoProvider parent) {
        return new OxmaFunctionInfoProvider(functionTable, parent);
    }

    public Optional<OxmaFunctionInfo> getFunctionInfo(String name, List<Type> signature) {
        Optional<OxmaFunctionInfo> functionInfo = functionTable.getFunctionInfo(name, signature);
        if (functionInfo.isPresent()) {
            return functionInfo;
        }
        if (parent == null) {
            return Optional.empty();
        }
        return parent.getFunctionInfo(name, signature);
    }
}
