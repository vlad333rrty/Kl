package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.RHS;
import kalina.compiler.expressions.VariableInfo;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            for (int i = 0; i < lhs.getVars().size(); i++) {
                rhs.getExpressions().get(i).translateToBytecode(methodVisitor);
                VariableInfo info = lhs.getVars().get(i);
                methodVisitor.visitVarInsn(info.getType().getOpcode(Opcodes.ISTORE), info.getIndex());
            }
        } else {
            throw new IllegalArgumentException("Either mv or cw should be present");
        }
    }
}
