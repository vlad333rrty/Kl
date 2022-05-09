package kalina.compiler.expressions.v2.funCall;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.ValueExpression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class PrintlnExpression extends AbstractFunCallExpression {
    public PrintlnExpression(List<Expression> arguments) {
        super(arguments);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        for (Expression expression : arguments) {
            printExpr(mv, expression);
            printExpr(mv, new ValueExpression(" ", Type.getType(String.class)));
        }
        printExpr(mv, new ValueExpression("\n", Type.getType(String.class)));
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }

    private void printExpr(MethodVisitor methodVisitor, Expression expression) throws CodeGenException {
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
}
