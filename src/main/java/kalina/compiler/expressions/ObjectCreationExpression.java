package kalina.compiler.expressions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kalina.compiler.codegen.CodeGenUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ObjectCreationExpression extends Expression {
    private final String className;
    private final List<Expression> arguments;

    public ObjectCreationExpression(String className, List<Expression> arguments) {
        this.className = className;
        this.arguments = arguments;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) {
        mv.visitTypeInsn(Opcodes.NEW, className);
        mv.visitInsn(Opcodes.DUP);
        for (Expression expression : arguments) {
            expression.translateToBytecode(mv);
        }
        String descriptor = CodeGenUtils
                .buildDescriptor(arguments.stream().map(Expression::getType).collect(Collectors.toList()), Optional.empty());
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", descriptor, false);
    }

    @Override
    public Type getType() {
        return Type.getObjectType(className);
    }
}
