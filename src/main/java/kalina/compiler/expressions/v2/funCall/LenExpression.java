package kalina.compiler.expressions.v2.funCall;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class LenExpression extends AbstractFunCallExpression {
    public LenExpression(List<Expression> arguments) {
        super(arguments);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("No suitable definition found for \"len\"");
        }
        Expression expression = arguments.get(0);
        if (expression.getType().getSort() == Type.ARRAY) {
            expression.translateToBytecode(mv);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
        } else {
            throw new IllegalArgumentException("Cannot apply 'len'. Incompatible type: array expected, got " + expression.getType().getClassName());
        }
    }

    @Override
    public Type getType() {
        return Type.INT_TYPE;
    }

    @Override
    public AbstractFunCallExpression substituteArguments(List<Expression> arguments) {
        return new LenExpression(arguments);
    }

    @Override
    public String toString() {
        return "len(" + arguments + ")";
    }
}
