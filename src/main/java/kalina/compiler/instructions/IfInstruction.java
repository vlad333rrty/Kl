package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.expressions.CondExpression;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class IfInstruction extends Instruction {
    private final CondExpression condition;
    private final Optional<AbstractBasicBlock> ifBranch, elseBranch;

    public IfInstruction(CondExpression condition, Optional<AbstractBasicBlock> ifBranch, Optional<AbstractBasicBlock> elseBranch) {
        this.condition = condition;
        this.ifBranch = ifBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            condition.translateToBytecode(methodVisitor);
            Label label = condition.getLabel();
            Label end = new Label();
            TranslationUtils.translateBlock(ifBranch, mv, cw);
            if (elseBranch.isPresent()) {
                methodVisitor.visitJumpInsn(Opcodes.GOTO, end);
            }
            methodVisitor.visitLabel(label);
            TranslationUtils.translateBlock(elseBranch, mv, cw);
            if (elseBranch.isPresent()) {
                methodVisitor.visitJumpInsn(Opcodes.GOTO, end);
                methodVisitor.visitLabel(end);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
}
