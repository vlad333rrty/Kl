package kalina.compiler.instructions.v2.assign;

import java.util.List;

import kalina.compiler.cfg.data.AssignArrayVariableInfo;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.array.AbstractArrayExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FieldArrayElementAssignInstruction extends AbstractAssignInstruction implements AbstractArrayExpression, ArrayElementAssign {
    public FieldArrayElementAssignInstruction(List<VariableInfo> lhs, List<Expression> rhs) {
        super(lhs, rhs);
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        return withRHS(expressions);
    }

    @Override
    protected void visitBeforeRHS(MethodVisitor mv, VariableInfo variableInfo) throws CodeGenException {
        OxmaFieldInfo fieldInfo = variableInfo.getFieldInfo().orElseThrow();
        int opcode = fieldInfo.isStatic() ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        if (!fieldInfo.isStatic()) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }
        mv.visitFieldInsn(opcode, fieldInfo.ownerClassName(), fieldInfo.fieldName(), fieldInfo.type().getDescriptor());
        translateElementsAccess(mv, variableInfo.getArrayVariableInfoOrElseThrow().getIndices());
    }

    @Override
    protected void visitStore(MethodVisitor mv, VariableInfo variableInfo) {
        mv.visitInsn(variableInfo.getArrayVariableInfoOrElseThrow().getElementType().getOpcode(Opcodes.IASTORE));
    }

    @Override
    public AbstractAssignInstruction withRHS(List<Expression> rhs) {
        assert getRhs().size() == rhs.size();
        return new FieldArrayElementAssignInstruction(getLhs(), rhs);
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
            builder.append(variableInfo.getName());
            for (Expression e : arrayVariableInfo.getIndices()) {
                builder.append("[").append(e).append("]");
            }
            builder.append(",");
        }
        return builder + " = " + PrintUtils.listToString(getRhs());
    }

    @Override
    public List<AssignArrayVariableInfo> getAssignArrayVariableInfo() {
        return getLhs().stream().map(VariableInfo::getArrayVariableInfoOrElseThrow).toList();
    }
}
