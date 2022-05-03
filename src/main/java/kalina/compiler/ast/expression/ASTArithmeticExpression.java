package kalina.compiler.ast.expression;

import java.util.List;

import kalina.compiler.expressions.operations.ArithmeticOperation;

/**
 * @author vlad333rrty
 */
public class ASTArithmeticExpression implements ASTExpression {
    private final List<ASTTerm> terms;
    private final List<ArithmeticOperation> operations;

    public ASTArithmeticExpression(List<ASTTerm> terms, List<ArithmeticOperation> operations) {
        this.terms = terms;
        this.operations = operations;
    }

    @Override
    public String toString() {
        return complexExpressionToString(terms, operations);
    }

    public List<ASTTerm> terms() {
        return terms;
    }

    public List<ArithmeticOperation> operations() {
        return operations;
    }
}
