package kalina.compiler.expressions;

import java.util.List;

import kalina.compiler.codegen.TypeCastOpcodesMapper;
import kalina.compiler.expressions.operations.ArithmeticOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArithmeticExpression extends Expression {
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
    public void translateToBytecode(MethodVisitor mv) {
        for (int i = 0; i < terms.size(); i++) {
            Term term = terms.get(i);
            term.translateToBytecode(mv);
            if (type.getSort() != term.getType().getSort()) {
                mv.visitInsn(TypeCastOpcodesMapper.getCastOpcode(term.getType(), type));
            }
            if (i > 0) {
                switch (operations.get(i - 1)) {
                    case PLUS:
                        mv.visitInsn(type.getOpcode(Opcodes.IADD));
                        break;
                    case MINUS:
                        mv.visitInsn(type.getOpcode(Opcodes.ISUB));
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected operation in Ar Expr: " + operations.get(i - 1));
                }
            }
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public List<ArithmeticOperation> getOperations() {
        return operations;
    }
}
