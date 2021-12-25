package kalina.compiler.syntax.parser.data;

/**
 * @author vlad333rrty
 */
public interface ILocalVariableTableFactory {
    ILocalVariableTable createLocalVariableTableForNonStatic();
    ILocalVariableTable createLocalVariableTableForStatic();
}
