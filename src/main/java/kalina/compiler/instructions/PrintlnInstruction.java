package kalina.compiler.instructions;

import java.util.List;
import java.util.Optional;

import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.ValueExpression;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class PrintlnInstruction extends Instruction {
    private final List<Expression> expressions;

    public PrintlnInstruction(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            for (Expression expression : expressions) {
                printExpr(methodVisitor, expression);
                printExpr(methodVisitor, new ValueExpression(" ", Type.getType(String.class)));
            }
            printExpr(methodVisitor, new ValueExpression("\n", Type.getType(String.class)));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void printExpr(MethodVisitor methodVisitor, Expression expression) {
        String descriptor = String.format("(%s)V", expression.getType().getDescriptor());
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        expression.translateToBytecode(methodVisitor);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", descriptor, false);
    }
}
