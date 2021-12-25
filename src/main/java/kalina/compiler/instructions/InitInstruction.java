package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.RHS;
import kalina.compiler.expressions.VariableInfo;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class InitInstruction extends Instruction {
    private final LHS lhs;
    private final Optional<RHS> rhs;
    private final Label start, end;

    public InitInstruction(LHS lhs, Optional<RHS> rhs, Label start, Label end) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.start = start;
        this.end = end;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            for (VariableInfo info : lhs.getVars()) {
                expressionCodeGen.createVarDecl(methodVisitor, info.getName(), info.getType().getDescriptor(), null, start, end, info.getIndex());
            }
            if (rhs.isPresent()) {
                for (int i = 0; i < lhs.getVars().size(); i++) {
                    rhs.get().getExpressions().get(i).translateToBytecode(methodVisitor);
                    VariableInfo info = lhs.getVars().get(i);
                    methodVisitor.visitVarInsn(info.getType().getOpcode(Opcodes.ISTORE), info.getIndex());
                }
            }
        } else {
            throw new IllegalArgumentException("Either mv or cw should be present");
        }
    }

    public Label getStart() {
        return start;
    }

    public Label getEnd() {
        return end;
    }
}
