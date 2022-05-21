package kalina.compiler.instructions;

import java.util.List;
import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.utils.PrintUtils;
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
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
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

    private void printExpr(MethodVisitor methodVisitor, Expression expression) throws CodeGenException {
        String descriptor = getDescriptor(expression.getType());
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        expression.translateToBytecode(methodVisitor);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", descriptor, false);
    }

    private String getDescriptor(Type type) {
        String argumentDescriptor = type.getSort() == Type.SHORT ? Type.INT_TYPE.getDescriptor() : type.getDescriptor();
        return String.format("(%s)V", argumentDescriptor);
    }

    @Override
    public String toString() {
        return "println" + PrintUtils.listToString(expressions);
    }
}
