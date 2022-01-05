package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.ReturnValueInfo;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FunEndInstruction extends Instruction {
    private final Optional<ReturnValueInfo> returnValueInfo;

    public FunEndInstruction(Optional<ReturnValueInfo> returnValueInfo) {
        this.returnValueInfo = returnValueInfo;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            if (returnValueInfo.isPresent()) {
                ReturnValueInfo info = returnValueInfo.get();
                Type type = info.getReturnType();
                Expression value = info.getReturnValue();
                value.translateToBytecode(methodVisitor);
                if (!type.equals(value.getType())) {
                    expressionCodeGen.cast(value.getType(), type, methodVisitor);
                }
                methodVisitor.visitInsn(type.getOpcode(Opcodes.IRETURN));
            } else {
                methodVisitor.visitInsn(Opcodes.RETURN);
            }
            methodVisitor.visitMaxs(0, 0); // this just forces asm to recompute stack size, arguments are picked randomly
            methodVisitor.visitEnd();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
