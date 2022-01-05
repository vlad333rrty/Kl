package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.CondExpression;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class DoInstruction extends Instruction {
    private final Optional<AbstractBasicBlock> entry;
    private final CondExpression condition;

    public DoInstruction(Optional<AbstractBasicBlock> entry, CondExpression condition) {
        this.entry = entry;
        this.condition = condition;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isEmpty()) {
            throw new IllegalArgumentException();
        }
        CondExpression neg = CondExpression.negate(condition);
        MethodVisitor methodVisitor = mv.get();
        Label label = neg.getLabel();
        methodVisitor.visitLabel(label);
        TranslationUtils.translateBlock(entry, mv, cw);
        neg.translateToBytecode(mv.get());
    }
}
