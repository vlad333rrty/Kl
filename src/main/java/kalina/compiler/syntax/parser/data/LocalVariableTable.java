package kalina.compiler.syntax.parser.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class LocalVariableTable implements ILocalVariableTable {
    private final Map<String, TypeAndIndex> variableTable;
    private final IndexGenerator indexGenerator;
    private ILocalVariableTable parent;

    public LocalVariableTable(IndexGenerator indexGenerator) {
        this.variableTable = new HashMap<>();
        this.indexGenerator = indexGenerator;
    }

    @Override
    public void addVariable(String name, Type type) {
        if (hasVariable(name)) {
            throw new IllegalArgumentException("Variable is already present in the table: " + name);
        }
        variableTable.put(name, new TypeAndIndex(type, indexGenerator.getNewIndex()));
    }

    @Override
    public Optional<TypeAndIndex> getTypeAndIndex(String name) {
        return Optional.ofNullable(variableTable.get(name));
    }

    @Override
    public boolean hasVariable(String name) {
        return variableTable.containsKey(name);
    }

    @Override
    public void setParent(ILocalVariableTable parent) {
        this.parent = parent;
    }
}
