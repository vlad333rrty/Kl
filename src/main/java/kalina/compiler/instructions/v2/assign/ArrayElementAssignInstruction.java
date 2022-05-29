package kalina.compiler.instructions.v2.assign;

import java.util.List;

import kalina.compiler.cfg.data.AssignArrayVariableInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.array.AbstractArrayExpression;
import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArrayElementAssignInstruction extends AbstractAssignInstruction implements AbstractArrayExpression, ArrayElementAssign {
    public ArrayElementAssignInstruction(List<VariableInfo> lhs, List<Expression> rhs) {
        super(lhs, rhs);
    }

    @Override
    protected void visitBeforeRHS(MethodVisitor mv, VariableInfo variableInfo) throws CodeGenException {
        expressionCodeGen.loadVariable(mv, variableInfo.getType().getOpcode(Opcodes.ILOAD), variableInfo.getIndexOrElseThrow());
        translateElementsAccess(mv, variableInfo.getArrayVariableInfoOrElseThrow().getIndices());
    }

    @Override
    protected void visitStore(MethodVisitor mv, VariableInfo variableInfo) {
        mv.visitInsn(variableInfo.getArrayVariableInfoOrElseThrow().getElementType().getOpcode(Opcodes.IASTORE));
    }

    @Override
    public AbstractAssignInstruction withRHS(List<Expression> rhs) {
        assert getRhs().size() == rhs.size();
        return new ArrayElementAssignInstruction(getLhs(), rhs);
    }

    @Override
    protected void performCastIfNeeded(MethodVisitor mv, VariableInfo variableInfo, Type expressionType) throws CodeGenException {
        Type lhsType = variableInfo.getArrayVariableInfoOrElseThrow().getLoweredType();
        if (!lhsType.equals(expressionType)) {
            expressionCodeGen.cast(expressionType, lhsType, mv);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        List<VariableInfo> lhs = getLhs();
        for (VariableInfo variableInfo : lhs) {
            AssignArrayVariableInfo arrayVariableInfo = variableInfo.getArrayVariableInfoOrElseThrow();
            builder.append(variableInfo.getIR());
            for (Expression e : arrayVariableInfo.getIndices()) {
                builder.append("[").append(e).append("]");
            }
            builder.append(",");
        }
        return builder + " = " + PrintUtils.listToString(getRhs());
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        assert getRhs().size() == expressions.size();
        return new ArrayElementAssignInstruction(getLhs(), expressions);
    }

    @Override
    public List<AssignArrayVariableInfo> getAssignArrayVariableInfo() {
        return getLhs().stream().map(VariableInfo::getArrayVariableInfoOrElseThrow).toList();
    }
}
