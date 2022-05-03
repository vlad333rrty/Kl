package kalina.compiler.syntax.parser2.data;

import java.util.List;
import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public interface OxmaFunctionTable {
    void addFunction(String name, OxmaFunctionInfo functionInfo);
    Optional<OxmaFunctionInfo> getFunctionInfo(String name, List<Type> signature);
    boolean hasFunction(String name, OxmaFunctionInfo functionInfo);
    void setParent(OxmaFunctionTable parent);
}
