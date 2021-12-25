package kalina.compiler.syntax.parser.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import kalina.compiler.bb.TypeAndName;

/**
 * @author vlad333rrty
 */
public class FunctionTable implements IFunctionTable {
    private final Map<String, FunctionInfo> functionTable = new HashMap<>();
    private IFunctionTable parent;

    @Override
    public void addFunction(String name, FunctionInfo functionInfo) {
        if (hasFunction(name, functionInfo)) {
            throw new IllegalArgumentException("Multiple function definition");
        }
        functionTable.put(name, functionInfo);
    }

    @Override
    public Optional<FunctionInfo> getFunctionInfo(String name) {
        return Optional.ofNullable(functionTable.get(name));
    }

    @Override
    public boolean hasFunction(String name, FunctionInfo functionInfo) {
        FunctionInfo presentInfo = functionTable.get(name);
        return presentInfo != null && hasSimilarSignature(presentInfo.getArguments(), functionInfo.getArguments());
    }

    @Override
    public void setParent(IFunctionTable parent) {
        this.parent = parent;
    }

    private boolean hasSimilarSignature(List<TypeAndName> args1, List<TypeAndName> args2) {
        if (args1.size() != args2.size()) {
            return false;
        }
        for (int i = 0; i < args1.size(); i++) {
            if (args1.get(i).getType().getSort() != args2.get(i).getType().getSort()) {
                return false;
            }
        }

        return true;
    }
}
