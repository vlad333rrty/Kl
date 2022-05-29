package kalina.compiler.instructions.v2.assign;

import java.util.List;

import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FieldAssignInstruction extends AbstractAssignInstruction {
    public FieldAssignInstruction(List<VariableInfo> lhs, List<Expression> rhs) {
        super(lhs, rhs);
    }

    @Override
    protected void visitBeforeRHS(MethodVisitor mv, VariableInfo variableInfo) {
        OxmaFieldInfo fieldInfo = variableInfo.getFieldInfo().orElseThrow();
        if (!fieldInfo.isStatic()) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }
    }

    @Override
    protected void visitStore(MethodVisitor mv, VariableInfo variableInfo) {
        OxmaFieldInfo fieldInfo = variableInfo.getFieldInfo().orElseThrow();
        int putFieldOpcode = fieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC)
                ? Opcodes.PUTSTATIC
                : Opcodes.PUTFIELD;
        mv.visitFieldInsn(putFieldOpcode, fieldInfo.ownerClassName(), fieldInfo.fieldName(), fieldInfo.type().getDescriptor());
    }

    @Override
    public AbstractAssignInstruction withRHS(List<Expression> rhs) {
        assert getRhs().size() == rhs.size();
        return new FieldAssignInstruction(getLhs(), rhs);
    }

    @Override
    protected void performCastIfNeeded(MethodVisitor mv, VariableInfo variableInfo, Type expressionType) throws CodeGenException {
        Type lhsType = variableInfo.getType();
        if (!lhsType.equals(expressionType)) {
            expressionCodeGen.cast(expressionType, lhsType, mv);
        }
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        return withRHS(expressions);
    }

    @Override
    public String toString() {
        return PrintUtils.listToString(getLhs().stream().map(VariableInfo::getName).toList())
                + " = " + PrintUtils.listToString(getRhs());
    }
}
