package kalina.compiler.syntax.parser.data;

import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class LocalVariableTable extends AbstractLocalVariableTable {
    public LocalVariableTable(IndexGenerator indexGenerator) {
        super(indexGenerator);
    }

    public LocalVariableTable(AbstractLocalVariableTable parent) {
        super(parent);
    }

    @Override
    public void addVariable(String name, Type type) {
        if (hasVariable(name)) {
            throw new IllegalArgumentException("Variable is already present in the table: " + name);
        }
        put(name, new TypeAndIndex(type, indexGenerator.getNewIndex(type)));
    }

    @Override
    public Optional<TypeAndIndex> findVariable(String name) {
        AbstractLocalVariableTable current = this;
        while (!current.hasVariable(name)) {
            if (current.parent == current) {
                return Optional.empty();
            }
            current = current.parent;
        }
        return Optional.of(current.get(name));
    }

    @Override
    public boolean hasVariable(String name) {
        return get(name) != null;
    }
}
