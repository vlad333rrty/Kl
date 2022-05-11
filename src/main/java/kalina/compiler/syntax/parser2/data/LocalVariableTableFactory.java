package kalina.compiler.syntax.parser2.data;

/**
 * @author vlad333rrty
 */
public class LocalVariableTableFactory implements ILocalVariableTableFactory {
    @Override
    public AbstractLocalVariableTable createLocalVariableTableForNonStatic() {
        return new LocalVariableTable(new SimpleIndexGenerator(1));
    }

    @Override
    public AbstractLocalVariableTable createLocalVariableTableForStatic() {
        return new LocalVariableTable(new SimpleIndexGenerator(0));
    }

    @Override
    public AbstractLocalVariableTable createChildLocalVariableTable(AbstractLocalVariableTable parent) {
        return new LocalVariableTable(parent);
    }
}
