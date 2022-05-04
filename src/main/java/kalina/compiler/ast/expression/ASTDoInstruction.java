package kalina.compiler.ast.expression;

import kalina.compiler.ast.ASTMethodEntryNode;

/**
 * @author vlad333rrty
 */
public class ASTDoInstruction implements ASTExpression {
    private final ASTMethodEntryNode entry;
    private final ASTCondExpression condition;

    public ASTDoInstruction(ASTMethodEntryNode entry, ASTCondExpression condition) {
        this.entry = entry;
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "do {\n\t" + entry + "\n} while " + condition + "\n";
    }

    public ASTMethodEntryNode entry() {
        return entry;
    }

    public ASTCondExpression condition() {
        return condition;
    }
}
