package kalina.compiler.cfg.data;

/**
 * @author vlad333rrty
 */
public interface ILocalVariableTableFactory {
    AbstractLocalVariableTable createLocalVariableTableForNonStatic();
    AbstractLocalVariableTable createLocalVariableTableForStatic();
}
