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
    public void translateToBytecode(MethodVisitor mv) {
        for (int i = 0; i < factors.size(); i++) {
            Factor factor = factors.get(i);
            factor.translateToBytecode(mv);
            if (type.getSort() != factor.getType().getSort()) {
                mv.visitInsn(TypeCastOpcodesMapper.getCastOpcode(factor.getType(), type));
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
}