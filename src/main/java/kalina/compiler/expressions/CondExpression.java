package kalina.compiler.expressions;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.operations.ComparisonOperation;
import kalina.compiler.expressions.v2.WithSubstitutableExpressions;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class CondExpression extends Expression implements WithSubstitutableExpressions<Expression> {
    private final List<Expression> expressions;
    private final List<ComparisonOperation> operations;
    private final Label label;

    private final boolean invert;

    public CondExpression(List<Expression> expressions, List<ComparisonOperation> operations) {
        this.expressions = expressions;
        this.operations = operations;
        this.label = new Label();
        this.invert = true;
    }

    private CondExpression(List<Expression> expressions, List<ComparisonOperation> operations, Label label, boolean shouldInvert) {
        this.expressions = expressions;
        this.operations = operations;
        this.label = label;
        this.invert = shouldInvert;
    }

    public static CondExpression negate(CondExpression condExpression) {
        return new CondExpression(condExpression.expressions, condExpression.operations, condExpression.label, !condExpression.invert);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        for (int i = 0; i < expressions.size(); i++) {
            Expression expression = expressions.get(i);
            expression.translateToBytecode(mv);
            if (i > 0) {
                switch (operations.get(i - 1)) {
                    case LESS:
                        mv.visitJumpInsn(invert ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPLT, label);
                        break;
                    case LESS_OR_EQUAL:
                        mv.visitJumpInsn(invert ? Opcodes.IF_ICMPGT : Opcodes.IF_ICMPLE, label);
                        break;
                    case GREATER:
                        mv.visitJumpInsn(invert ? Opcodes.IF_ICMPLE : Opcodes.IF_ICMPGT, label);
                        break;
                    case GREATER_OR_EQUAL:
                        mv.visitJumpInsn(invert ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGE, label);
                        break;
                    case EQUAL:
                        mv.visitJumpInsn(invert ? Opcodes.IF_ICMPNE : Opcodes.IF_ICMPEQ, label);
                        break;
                    case NOT_EQUAL:
                        mv.visitJumpInsn(invert ? Opcodes.IF_ICMPEQ : Opcodes.IF_ICMPNE, label);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown comparison operation");
                }
            }
        }
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN_TYPE;
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return PrintUtils.complexExpressionToString(expressions, operations) + " ?";
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public CondExpression substituteExpressions(List<Expression> expressions) {
        assert this.expressions.size() == expressions.size();
        return new CondExpression(expressions, operations, label, invert);
    }
}
