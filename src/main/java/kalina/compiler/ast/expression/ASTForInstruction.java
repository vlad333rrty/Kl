package kalina.compiler.ast.expression;

import java.util.Optional;

import kalina.compiler.ast.ASTMethodEntryNode;

/**
 * @author vlad333rrty
 */
public final class ASTForInstruction implements ASTExpression {
    private final Optional<ASTExpression> declarations;
    private final Optional<ASTCondExpression> condition;
    private final Optional<ASTExpression> action;
    private final ASTMethodEntryNode entry;

    public ASTForInstruction(
            Optional<ASTExpression> declarations,
            Optional<ASTCondExpression> condition,
            Optional<ASTExpression> action,
            ASTMethodEntryNode entry)
    {
        this.declarations = declarations;
        this.condition = condition;
        this.action = action;
        this.entry = entry;
    }

    @Override
    public String toString() {
        return "for " + (declarations.isPresent() ? declarations : "") + ";" +
                (condition.isPresent() ? condition : "") + ";" +
                (action.isPresent() ? action : "") + "{\n\t" +
                entry.toString() + "\n}";
    }

    public Optional<ASTExpression> declarations() {
        return declarations;
    }

    public Optional<ASTCondExpression> condition() {
        return condition;
    }

    public Optional<ASTExpression> action() {
        return action;
    }

    public ASTMethodEntryNode entry() {
        return entry;
    }
}
