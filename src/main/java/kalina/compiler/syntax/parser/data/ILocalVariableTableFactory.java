package kalina.compiler.syntax.parser.data;

/**
 * @author vlad333rrty
 */
public interface ILocalVariableTableFactory {
    AbstractLocalVariableTable createLocalVariableTableForNonStatic();
    AbstractLocalVariableTable createLocalVariableTableForStatic();
    AbstractLocalVariableTable createChildLocalVariableTable(AbstractLocalVariableTable parent);
}
