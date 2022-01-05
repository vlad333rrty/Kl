package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.RHS;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class AssignInstruction extends Instruction {
    private final LHS lhs;
    private final RHS rhs;

    public AssignInstruction(LHS lhs, RHS rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            Type type = lhs.getType();
            for (Expression expression : rhs.getExpressions()) {
                expression.translateToBytecode(methodVisitor);
                Type expressionType = expression.getType();
                if (!type.equals(expressionType)) {
                    expressionCodeGen.cast(expressionType, type, methodVisitor);
                }
            }
            for (int i = lhs.getVars().size() - 1; i >= 0; i--) {
                methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ISTORE), lhs.getVars().get(i).getIndex());
            }
        } else {
            throw new IllegalArgumentException("Either mv or cw should be present");
        }
    }
}
