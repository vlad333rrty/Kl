package kalina.compiler.expressions;

import java.util.List;

import kalina.compiler.codegen.typeCast.NumberTypesComparator;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.operations.ArithmeticOperation;
import kalina.compiler.expressions.v2.WithSubstitutableExpressions;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArithmeticExpression extends Expression implements WithSubstitutableExpressions<Term> {
    private final List<Term> terms;
    private final List<ArithmeticOperation> operations;
    private Type type;

    public ArithmeticExpression(List<Term> terms, List<ArithmeticOperation> operations) {
        this.terms = terms;
        this.operations = operations;
        this.type = terms.stream().findFirst().orElseThrow().getType();
        for (Term term : terms) {
            if (NumberTypesComparator.isNumber(type)) {
                type = NumberTypesComparator.max(type, term.getType());
            }
        }
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        for (int i = 0; i < terms.size(); i++) {
            Term term = terms.get(i);
            term.translateToBytecode(mv);
            if (!type.equals(term.getType())) {
                expressionCodeGen.cast(term.getType(), type, mv);
            }
            if (i > 0) {
                switch (operations.get(i - 1)) {
                    case PLUS -> mv.visitInsn(type.getOpcode(Opcodes.IADD));
                    case MINUS -> mv.visitInsn(type.getOpcode(Opcodes.ISUB));
                    default -> throw new IllegalArgumentException("Unexpected operation in Ar Expr: " + operations.get(i - 1));
                }
            }
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return PrintUtils.complexExpressionToString(terms, operations);
    }

    public List<Term> getTerms() {
        return terms;
    }

    public ArithmeticExpression withTerms(List<Term> terms) {
        assert this.terms.size() == terms.size();
        return new ArithmeticExpression(terms, operations);
    }

    @Override
    public Expression substituteExpressions(List<Term> expressions) {
        assert this.terms.size() == expressions.size();
        return new ArithmeticExpression(expressions, operations);
    }

    public List<ArithmeticOperation> getOperations() {
        return operations;
    }
}
