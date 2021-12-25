package kalina.compiler.syntax.parser.data;

import java.util.Optional;

/**
 * @author vlad333rrty
 */
public interface IFunctionTable {
    void addFunction(String name, FunctionInfo functionInfo);
    Optional<FunctionInfo> getFunctionInfo(String name);
    boolean hasFunction(String name, FunctionInfo functionInfo);
    void setParent(IFunctionTable parent);
}
