package kalina.compiler.ast.expression;

import kalina.compiler.ast.ASTBranchExpressionConversionFactory;
import kalina.compiler.ast.ASTMethodEntryNode;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;

/**
 * @author vlad333rrty
 */
public class ASTDoInstruction implements ASTExpression, ASTBranchExpression {
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

    @Override
    public <T> T convert(ASTBranchExpressionConversionFactory<T> conversionFactory) throws CFGConversionException, IncompatibleTypesException {
        return conversionFactory.convertDo(this);
    }
}
