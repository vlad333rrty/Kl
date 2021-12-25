package kalina.compiler.expressions;

import java.util.List;
import java.util.stream.Collectors;

import kalina.compiler.bb.TypeAndName;
import kalina.compiler.codegen.CodeGenUtils;
import kalina.compiler.syntax.parser.data.FunctionInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FunCallExpression extends Expression {
    private final String funName;
    private final List<Expression> expressions;
    private final FunctionInfo functionInfo;

    public FunCallExpression(String funName, List<Expression> expressions, FunctionInfo functionInfo) {
        this.funName = funName;
        this.expressions = expressions;
        this.functionInfo = functionInfo;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) {
        for (Expression expression : expressions) {
            expression.translateToBytecode(mv);
        }
        String descriptor = CodeGenUtils.buildDescriptor(
                functionInfo.getArguments().stream().map(TypeAndName::getType).collect(Collectors.toList()),
                functionInfo.getReturnType());
        int opcode = functionInfo.isStatic() ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
        mv.visitMethodInsn(opcode, functionInfo.getOwnerClass(), funName, descriptor, false);
    }

    @Override
    public Type getType() {
        return functionInfo.getReturnType().orElse(Type.VOID_TYPE);
    }
}
