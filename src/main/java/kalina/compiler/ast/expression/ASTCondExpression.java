package kalina.compiler.ast.expression;

import java.util.List;

import kalina.compiler.expressions.operations.ComparisonOperation;

/**
 * @author vlad333rrty
 */
public class ASTCondExpression implements ASTExpression {
    private final List<ASTExpression> expressions;
    private final List<ComparisonOperation> operations;

    public ASTCondExpression(List<ASTExpression> expressions, List<ComparisonOperation> operations) {
        this.expressions = expressions;
        this.operations = operations;
    }

    @Override
    public String toString() {
        return complexExpressionToString(expressions, operations);
    }

    public List<ASTExpression> expressions() {
        return expressions;
    }

    public List<ComparisonOperation> operations() {
        return operations;
    }
}
