package kalina.compiler.instructions.v2;

import java.util.List;

import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.expressions.Expression;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class AssignInstruction extends AbstractAssignInstruction {
    public AssignInstruction(List<VariableInfo> lhs, List<Expression> rhs) {
        super(lhs, rhs);
    }

    @Override
    protected void visitBeforeRHS(MethodVisitor mv, VariableInfo variableInfo) {
        // do nothing
    }

    @Override
    protected void visitStore(MethodVisitor mv, VariableInfo variableInfo) {
        mv.visitVarInsn(variableInfo.getType().getOpcode(Opcodes.ISTORE), variableInfo.getIndex());
    }

    @Override
    public AbstractAssignInstruction withRHS(List<Expression> rhs) {
        assert getRhs().size() == rhs.size();
        return new AssignInstruction(getLhs(), rhs);
    }

    @Override
    public String toString() {
        return PrintUtils.listToString(getLhs().stream().map(VariableInfo::toString).toList())
                + " = " + PrintUtils.listToString(getRhs());
    }
}
