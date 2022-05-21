package kalina.compiler.ast.expression;

import java.util.Optional;

import kalina.compiler.ast.ASTBranchExpressionConversionFactory;
import kalina.compiler.ast.ASTMethodEntryNode;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;

/**
 * @author vlad333rrty
 */
public class ASTIfInstruction implements ASTExpression, ASTBranchExpression {
    private final ASTCondExpression condExpression;
    private final ASTMethodEntryNode thenBr;
    private final Optional<ASTMethodEntryNode> elseBr;

    public ASTIfInstruction(
            ASTCondExpression condExpression,
            ASTMethodEntryNode thenBr,
            Optional<ASTMethodEntryNode> elseBr)
    {
        this.condExpression = condExpression;
        this.thenBr = thenBr;
        this.elseBr = elseBr;
    }

    @Override
    public String toString() {
        return "if " + condExpression + "then " + thenBr + elseBr.map(astMethodEntryNode -> " else " + astMethodEntryNode).orElse("");
    }

    public ASTCondExpression condExpression() {
        return condExpression;
    }

    public ASTMethodEntryNode thenBr() {
        return thenBr;
    }

    public Optional<ASTMethodEntryNode> elseBr() {
        return elseBr;
    }

    @Override
    public <T> T convert(ASTBranchExpressionConversionFactory<T> conversionFactory) throws CFGConversionException, IncompatibleTypesException {
        return conversionFactory.convertIf(this);
    }
}
