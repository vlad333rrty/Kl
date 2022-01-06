package kalina.compiler.syntax.parser.data;

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
public class FunctionTable implements IFunctionTable {
    private final Map<String, List<FunctionInfo>> functionTable = new HashMap<>();
    private IFunctionTable parent;

    @Override
    public void addFunction(String name, FunctionInfo functionInfo) {
        if (hasFunction(name, functionInfo)) {
            throw new IllegalArgumentException("Multiple function definition");
        }
        functionTable.computeIfAbsent(name, k -> new ArrayList<>()).add(functionInfo);
    }

    @Override
    public Optional<FunctionInfo> getFunctionInfo(String name, List<Type> signature) {
        return functionTable.getOrDefault(name, List.of()).stream()
                .filter(info -> retrieveTypes(info.getArguments()).equals(signature))
                .findFirst();
    }

    @Override
    public boolean hasFunction(String name, FunctionInfo functionInfo) {
        List<FunctionInfo> presentInfo = functionTable.getOrDefault(name, List.of());
        return !presentInfo.isEmpty() && presentInfo.stream()
                .anyMatch(info -> retrieveTypes(info.getArguments()).equals(retrieveTypes(functionInfo.getArguments())));
    }

    @Override
    public void setParent(IFunctionTable parent) {
        this.parent = parent;
    }

    private List<Type> retrieveTypes(List<TypeAndName> typeAndNames) {
        return typeAndNames.stream().map(TypeAndName::getType).collect(Collectors.toList());
    }
}
