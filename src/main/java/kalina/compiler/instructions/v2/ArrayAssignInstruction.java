package kalina.compiler.instructions.v2;

import java.util.List;

import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.array.AbstractArrayExpression;
import kalina.compiler.syntax.parser.data.VariableInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public class ArrayAssignInstruction extends AbstractAssignInstruction implements AbstractArrayExpression {
    public ArrayAssignInstruction(List<VariableInfo> lhs, List<Expression> rhs) {
        super(lhs, rhs);
    }

    @Override
    protected void visitBeforeRHS(MethodVisitor mv, VariableInfo variableInfo) {
        expressionCodeGen.loadVariable(mv, variableInfo.getType().getOpcode(Opcodes.ILOAD), variableInfo.getIndex());
        translateElementsAccess(mv, variableInfo.getArrayVariableInfoOrElseThrow().getIndices());
    }

    @Override
    protected void visitStore(MethodVisitor mv, VariableInfo variableInfo) {
        mv.visitInsn(variableInfo.getArrayVariableInfoOrElseThrow().getElementType().getOpcode(Opcodes.IASTORE));
    }
}