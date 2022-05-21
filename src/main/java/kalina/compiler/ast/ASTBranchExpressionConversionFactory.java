package kalina.compiler.ast;

import kalina.compiler.ast.expression.ASTDoInstruction;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;

/**
 * @author vlad333rrty
 */
public interface ASTBranchExpressionConversionFactory<T> {
    T convertFor(ASTForInstruction forInstruction) throws CFGConversionException, IncompatibleTypesException;
    T convertDo(ASTDoInstruction doInstruction) throws CFGConversionException, IncompatibleTypesException;
    T convertIf(ASTIfInstruction ifInstruction) throws CFGConversionException, IncompatibleTypesException;
}
