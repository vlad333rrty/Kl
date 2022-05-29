package kalina.compiler.instructions.v2.assign;

import java.util.List;

import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithLHS;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class AssignInstruction extends AbstractAssignInstruction implements WithLHS {
    public AssignInstruction(List<VariableInfo> lhs, List<Expression> rhs) {
        super(lhs, rhs);
    }

    @Override
    protected void visitBeforeRHS(MethodVisitor mv, VariableInfo variableInfo) {
        // do nothing
    }

    @Override
    protected void visitStore(MethodVisitor mv, VariableInfo variableInfo) {
        if (variableInfo.getFieldInfo().isPresent()) {

        } else {
            mv.visitVarInsn(variableInfo.getType().getOpcode(Opcodes.ISTORE), variableInfo.getIndexOrElseThrow());
        }
    }

    @Override
    public AbstractAssignInstruction withRHS(List<Expression> rhs) {
        assert getRhs().size() == rhs.size();
        return new AssignInstruction(getLhs(), rhs);
    }

    @Override
    protected void performCastIfNeeded(MethodVisitor mv, VariableInfo variableInfo, Type expressionType) throws CodeGenException {
        Type lhsType = variableInfo.getType();
        if (!lhsType.equals(expressionType)) {
            expressionCodeGen.cast(expressionType, lhsType, mv);
        }
    }

    @Override
    public String toString() {
        return PrintUtils.listToString(getLhs().stream().map(VariableInfo::toString).toList())
                + " = " + PrintUtils.listToString(getRhs());
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        assert getRhs().size() == expressions.size();
        return new AssignInstruction(getLhs(), expressions);
    }

    @Override
    public List<SSAVariableInfo> getVariableInfos() {
        return getLhs().stream()
                .filter(variableInfo -> variableInfo.getFieldInfo().isEmpty())
                .map(VariableInfo::getSsaVariableInfo).toList();
    }
}
