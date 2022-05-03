package kalina.compiler.ast;

/**
 * @author vlad333rrty
 */
public class ASTMethodEntryNode extends ASTNodeBase {
    @Override
    public String toString() {
        return "entry{ children number=" + getExpressions().size() + " }";
    }
}
