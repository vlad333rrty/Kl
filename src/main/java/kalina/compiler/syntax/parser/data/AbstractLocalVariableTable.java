package kalina.compiler.syntax.parser.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public abstract class AbstractLocalVariableTable {
    protected final IndexGenerator indexGenerator;
    protected final AbstractLocalVariableTable parent;
    private final Map<String, TypeAndIndex> variableTable = new HashMap<>();

    public AbstractLocalVariableTable(IndexGenerator indexGenerator) {
        this.indexGenerator = indexGenerator;
        this.parent = this;
    }

    public AbstractLocalVariableTable(AbstractLocalVariableTable parent) {
        this.indexGenerator = parent.indexGenerator;
        this.parent = parent;
    }

    public abstract int addVariable(String name, Type type);

    public abstract Optional<TypeAndIndex> findVariable(String name);

    public abstract boolean hasVariable(String name);

    public abstract boolean hasVariableGlobal(String name);

    protected TypeAndIndex get(String name) {
        return variableTable.get(name);
    }

    protected void put(String name, TypeAndIndex value) {
        variableTable.put(name, value);
    }
}
