package kalina.compiler.ast.expression;

import kalina.compiler.ast.ASTBranchExpressionConversionFactory;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;

/**
 * @author vlad333rrty
 */
public interface ASTBranchExpression {
    <T> T convert(ASTBranchExpressionConversionFactory<T> conversionFactory) throws CFGConversionException, IncompatibleTypesException;
}
