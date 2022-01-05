package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.RHS;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.VariableNameAndIndex;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class InitInstruction extends Instruction {
    private final LHS lhs;
    private final Optional<RHS> rhs;
    private final Label start, end;

    public InitInstruction(LHS lhs, Optional<RHS> rhs, Label start, Label end) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.start = start;
        this.end = end;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            Type type = lhs.getType();
            for (VariableNameAndIndex info : lhs.getVars()) {
                expressionCodeGen.createVarDecl(methodVisitor, info.getName(), type.getDescriptor(), null, start, end, info.getIndex());
            }
            if (rhs.isPresent()) {
                for (int i = 0; i < lhs.getVars().size(); i++) {
                    Expression expression = rhs.get().getExpressions().get(i);
                    expression.translateToBytecode(methodVisitor);
                    if (!type.equals(expression.getType())) {
                        expressionCodeGen.cast(expression.getType(), type, methodVisitor);
                    }
                    VariableNameAndIndex info = lhs.getVars().get(i);
                    methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ISTORE), info.getIndex());
                }
            } else {
                for (VariableNameAndIndex info : lhs.getVars()) {
                    new ValueExpression(getEmptyValue(type), type).translateToBytecode(methodVisitor);
                    methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ISTORE), info.getIndex());
                }
            }
        } else {
            throw new IllegalArgumentException("Either mv or cw should be present");
        }
    }

    private Object getEmptyValue(Type type) {
        switch (type.getSort()) {
            case Type.SHORT:
            case Type.INT:
                return 0;
            case Type.LONG:
                return 0L;
            case Type.FLOAT:
                return 0f;
            case Type.DOUBLE:
                return 0d;
            case Type.BOOLEAN:
                return false;
            case Type.OBJECT:
                return type.equals(Type.getType(String.class)) ? "" : null;
            default:
                throw new IllegalArgumentException("Unexpected type");
        }
    }
}
