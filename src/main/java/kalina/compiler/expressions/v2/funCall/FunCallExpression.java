package kalina.compiler.expressions.v2.funCall;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kalina.compiler.bb.TypeAndName;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenUtils;
import kalina.compiler.expressions.Expression;
import kalina.compiler.cfg.data.OxmaFunctionInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FunCallExpression extends AbstractFunCallExpression {
    private final String funName;
    private final OxmaFunctionInfo functionInfo;
    private final Optional<Expression> variableAccessExpression;

    public FunCallExpression(
            String funName,
            List<Expression> arguments,
            OxmaFunctionInfo functionInfo,
            Optional<Expression> variableAccessExpression)
    {
        super(arguments);
        this.funName = funName;
        this.functionInfo = functionInfo;
        this.variableAccessExpression = variableAccessExpression;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        if (variableAccessExpression.isPresent()) {
            variableAccessExpression.get().translateToBytecode(mv);
        }
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
