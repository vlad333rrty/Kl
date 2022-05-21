package kalina.compiler.cfg.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import kalina.compiler.bb.TypeAndName;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class OxmaFunctionTableImpl implements OxmaFunctionTable {
    private final Map<String, List<OxmaFunctionInfo>> functionTable = new HashMap<>();
    private OxmaFunctionTable parent;

    @Override
    public void addFunction(String name, OxmaFunctionInfo functionInfo) {
        if (hasFunction(name, functionInfo)) {
            throw new IllegalArgumentException("Multiple function definition");
        }
        functionTable.computeIfAbsent(name, k -> new ArrayList<>()).add(functionInfo);
    }

    @Override
    public Optional<OxmaFunctionInfo> getFunctionInfo(String name, List<Type> signature) {
        return functionTable.getOrDefault(name, List.of()).stream()
                .filter(info -> retrieveTypes(info.arguments()).equals(signature))
                .findFirst();
    }

    @Override
    public boolean hasFunction(String name, OxmaFunctionInfo functionInfo) {
        List<OxmaFunctionInfo> presentInfo = functionTable.getOrDefault(name, List.of());
        return !presentInfo.isEmpty() && presentInfo.stream()
                .anyMatch(info -> retrieveTypes(info.arguments()).equals(retrieveTypes(functionInfo.arguments())));
    }

    @Override
    public void setParent(OxmaFunctionTable parent) {
        this.parent = parent;
    }

    private List<Type> retrieveTypes(List<TypeAndName> typeAndNames) {
        return typeAndNames.stream().map(TypeAndName::getType).collect(Collectors.toList());
    }
}
