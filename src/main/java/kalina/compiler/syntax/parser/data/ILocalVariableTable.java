package kalina.compiler.syntax.parser.data;

import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public interface ILocalVariableTable {
    void addVariable(String name, Type type);
    Optional<TypeAndIndex> getTypeAndIndex(String name);
    boolean hasVariable(String name);
    void setParent(ILocalVariableTable parent);
}
