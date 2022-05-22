package kalina.compiler.expressions;

import java.util.List;

import kalina.compiler.codegen.typeCast.NumberTypesComparator;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.operations.ArithmeticOperation;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class Term extends Expression {
    private final List<Factor> factors;
    private final List<ArithmeticOperation> operations;
    private Type type;

    public Term(List<Factor> factors, List<ArithmeticOperation> operations) {
        this.factors = factors;
        this.operations = operations;
        this.type = factors.stream().findFirst().orElseThrow().getType();
        for (Factor factor : this.factors) {
            if (NumberTypesComparator.isNumber(type)) {
                type = NumberTypesComparator.max(type, factor.getType());
            }
        }
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        for (int i = 0; i < factors.size(); i++) {
            Factor factor = factors.get(i);
            factor.translateToBytecode(mv);
            if (!type.equals(factor.getType())) {
                expressionCodeGen.cast(factor.getType(), type, mv);
            }
            if (i > 0) {
                switch (operations.get(i - 1)) {
                    case MULTIPLY:
                        mv.visitInsn(type.getOpcode(Opcodes.IMUL));
                        break;
                    case DIVIDE:
                        mv.visitInsn(type.getOpcode(Opcodes.IDIV));
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected operation in Term: " + operations.get(i - 1));
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
        return PrintUtils.complexExpressionToString(factors, operations);
    }

    public List<Factor> getFactors() {
        return factors;
    }

    public Term withFactors(List<Factor> factors) {
        assert this.factors.size() == factors.size();
        return new Term(factors, operations);
    }
}
