package kalina.compiler.expressions.v2.funCall;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class PrintExpression extends AbstractFunCallExpression {
    public PrintExpression(List<Expression> arguments) {
        super(arguments);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
            Expression expression = arguments.get(i);
            printExpr(mv, expression);
            if (argumentsSize > 1 && i < argumentsSize - 1) {
                printExpr(mv, new ValueExpression(" ", Type.getType(String.class)));
            }
        }
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }

    protected void printExpr(MethodVisitor methodVisitor, Expression expression) throws CodeGenException {
        String descriptor = getDescriptor(expression.getType());
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        expression.translateToBytecode(methodVisitor);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", descriptor, false);
    }

    private String getDescriptor(Type type) {
        String descriptor = switch (type.getSort()) {
            case Type.SHORT, Type.INT -> Type.INT_TYPE.getDescriptor();
            case Type.ARRAY, Type.OBJECT -> "Ljava/lang/Object;";
            default -> type.getDescriptor();
        };
        return String.format("(%s)V", descriptor);
    }

    @Override
    public String toString() {
        return "print" + PrintUtils.listToString(arguments);
    }

    @Override
    public AbstractFunCallExpression substituteArguments(List<Expression> arguments) {
        return new PrintExpression(arguments);
    }
}
