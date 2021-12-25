package kalina.compiler.syntax.parser.data;

/**
 * @author vlad333rrty
 */
public class LocalVariableTableFactory implements ILocalVariableTableFactory {
    @Override
    public ILocalVariableTable createLocalVariableTableForNonStatic() {
        return new LocalVariableTable(new SimpleIndexGenerator(1));
    }

    @Override
    public ILocalVariableTable createLocalVariableTableForStatic() {
        return new LocalVariableTable(new SimpleIndexGenerator(0));
    }
}
