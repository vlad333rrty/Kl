package kalina.compiler.syntax.parser.data;

import java.util.List;
import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public interface IFunctionTable {
    void addFunction(String name, FunctionInfo functionInfo);
    Optional<FunctionInfo> getFunctionInfo(String name, List<Type> signature);
    boolean hasFunction(String name, FunctionInfo functionInfo);
    void setParent(IFunctionTable parent);
}
