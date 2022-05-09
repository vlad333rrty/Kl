package kalina.compiler.expressions.v2.funCall;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kalina.compiler.bb.TypeAndName;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenUtils;
import kalina.compiler.expressions.Expression;
import kalina.compiler.syntax.parser2.data.OxmaFunctionInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FunCallExpression extends AbstractFunCallExpression {
    private final String funName;
    private final OxmaFunctionInfo functionInfo;
    private final Optional<Integer> variableIndex;

    public FunCallExpression(
            String funName,
            List<Expression> arguments,
            OxmaFunctionInfo functionInfo,
            Optional<Integer> variableIndex)
    {
        super(arguments);
        this.funName = funName;
        this.functionInfo = functionInfo;
        this.variableIndex = variableIndex;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        variableIndex.ifPresent(index -> mv.visitVarInsn(Opcodes.ALOAD, index));
        for (Expression expression : arguments) {
            expression.translateToBytecode(mv);
        }
        String descriptor = CodeGenUtils.buildDescriptor(
                functionInfo.arguments().stream().map(TypeAndName::getType).collect(Collectors.toList()),
                Optional.of(functionInfo.returnType()));
        int opcode = functionInfo.isStatic() ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
        mv.visitMethodInsn(opcode, functionInfo.ownerClassName(), funName, descriptor, false);
    }

    @Override
    public Type getType() {
        return functionInfo.returnType();
    }
}
