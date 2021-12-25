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
public class ForInstruction extends Instruction {
    private final Optional<InitInstruction> declarations;
    private final Optional<CondExpression> condition;
    private final Optional<Instruction> action;
    private final Optional<AbstractBasicBlock> entry;

    public ForInstruction(
            Optional<InitInstruction> declarations,
            Optional<CondExpression> condition,
            Optional<Instruction> action,
            Optional<AbstractBasicBlock> entry)
    {
        this.declarations = declarations;
        this.condition = condition;
        this.action = action;
        this.entry = entry;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            if (declarations.isPresent()) {
                InitInstruction instruction = declarations.get();
                //methodVisitor.visitLabel(instruction.getStart());
                instruction.translateToBytecode(mv, cw);
            }

            Label start = new Label();
            Label end;
            if (condition.isPresent()) {
                CondExpression condExpression = condition.get();
                methodVisitor.visitLabel(start);
                condExpression.translateToBytecode(methodVisitor);
                end = condExpression.getLabel();
            } else {
                end = new Label();
            }

            translateBlock(entry, mv, cw);
            action.ifPresent(a -> a.translateToBytecode(mv, cw));
            methodVisitor.visitJumpInsn(Opcodes.GOTO, start);
            // todo check for empty condition

            methodVisitor.visitLabel(end);
            //declarations.ifPresent(initInstruction -> methodVisitor.visitLabel(initInstruction.getEnd()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void translateBlock(Optional<AbstractBasicBlock> bb, Optional<MethodVisitor> mv, Optional<ClassWriter> cw) {
        Optional<AbstractBasicBlock> current = bb;
        while (current.isPresent()) {
            current.get().getInstruction().translateToBytecode(mv, cw);
            current = current.get().getNext();
        }
    }
}
