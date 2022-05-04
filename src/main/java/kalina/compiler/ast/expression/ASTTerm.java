package kalina.compiler.ast.expression;

import java.util.List;

import kalina.compiler.expressions.operations.ArithmeticOperation;

/**
 * @author vlad333rrty
 */
public record ASTTerm(List<ASTFactor> factors, List<ArithmeticOperation> operations) implements ASTExpression {
    @Override
    public String toString() {
        return complexExpressionToString(factors, operations);
    }
}
