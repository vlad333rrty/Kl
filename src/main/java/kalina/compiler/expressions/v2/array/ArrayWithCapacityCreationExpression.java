package kalina.compiler.expressions.v2.array;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.WithSubstitutableExpressions;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArrayWithCapacityCreationExpression extends Expression implements AbstractArrayExpression, WithSubstitutableExpressions<Expression> {
    private final List<Expression> capacities;
    private final Type arrayType;
    private final Type type;

    public ArrayWithCapacityCreationExpression(List<Expression> capacities, Type arrayType, Type type) {
        this.capacities = capacities;
        this.arrayType = arrayType;
        this.type = type;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        for (Expression expression : capacities) {
            expression.translateToBytecode(mv);
        }
        if (capacities.size() == 1) {
            if (type.getSort() >= 1 && type.getSort() <= 8) {
                mv.visitIntInsn(Opcodes.NEWARRAY, getOpcode());
            } else {
                mv.visitTypeInsn(Opcodes.ANEWARRAY, type.getInternalName());
            }
        } else {
            mv.visitMultiANewArrayInsn(arrayType.getDescriptor(), capacities.size());
        }
    }

    private int getOpcode() {
        return switch (type.getSort()) {
            case Type.SHORT -> Opcodes.T_SHORT;
            case Type.INT -> Opcodes.T_INT;
            case Type.LONG -> Opcodes.T_LONG;
            case Type.FLOAT -> Opcodes.T_FLOAT;
            case Type.DOUBLE -> Opcodes.T_DOUBLE;
            case Type.BOOLEAN -> Opcodes.T_BOOLEAN;
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public Type getType() {
        return arrayType;
    }

    @Override
    public String toString() {
        return type + PrintUtils.listToString(capacities);
    }

    @Override
    public Expression substituteExpressions(List<Expression> expressions) {
        assert expressions.size() == capacities.size();
        return new ArrayWithCapacityCreationExpression(
                expressions,
                arrayType,
                type
        );
    }

    public List<Expression> getCapacities() {
        return capacities;
    }
}
