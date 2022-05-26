package kalina.compiler.instructions.v2;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.VariableNameAndIndex;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class InitInstruction extends Instruction implements WithExpressions, WithLHS {
    private final LHS lhs;
    private final List<Expression> rhs;

    public InitInstruction(LHS lhs, List<Expression> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            Type type = lhs.getType();
            for (VariableNameAndIndex info : lhs.getVars()) {
                expressionCodeGen.createVarDecl(methodVisitor, info.getName(), type.getDescriptor(), null, new Label(), new Label(), info.getIndex());
            }
            if (rhs.isEmpty()) {
                for (VariableNameAndIndex info : lhs.getVars()) {
                    new ValueExpression(getEmptyValue(type), type).translateToBytecode(methodVisitor);
                    methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ISTORE), info.getIndex());
                }
            } else {
                for (int i = 0; i < lhs.getVars().size(); i++) {
                    Expression expression = rhs.get(i);
                    expression.translateToBytecode(methodVisitor);
                    if (!type.equals(expression.getType())) {
                        expressionCodeGen.cast(expression.getType(), type, methodVisitor);
                    }
                    VariableNameAndIndex info = lhs.getVars().get(i);
                    methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ISTORE), info.getIndex());
                }
            }
        } else {
            throw new IllegalArgumentException("Either mv or cw should be present");
        }
    }

    public LHS getLhs() {
        return lhs;
    }

    public List<Expression> getRhs() {
        return rhs;
    }

    private Object getEmptyValue(Type type) {
        return switch (type.getSort()) {
            case Type.SHORT, Type.INT -> 0;
            case Type.LONG -> 0L;
            case Type.FLOAT -> 0f;
            case Type.DOUBLE -> 0d;
            case Type.BOOLEAN -> false;
            case Type.OBJECT -> type.equals(Type.getType(String.class)) ? "" : null;
            default -> throw new IllegalArgumentException("Unexpected type");
        };
    }

    @Override
    public String toString() {
        return lhs.toString() + " = " + PrintUtils.listToString(rhs);
    }

    public InitInstruction withRHS(List<Expression> expressions) {
        assert this.rhs.size() == expressions.size();
        return new InitInstruction(lhs, expressions);
    }

    @Override
    public List<Expression> getExpressions() {
        return rhs;
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        return new InitInstruction(lhs, expressions);
    }

    @Override
    public List<SSAVariableInfo> getVariableInfos() {
        return lhs.getVars().stream().map(VariableNameAndIndex::getSsaVariableInfo).toList();
    }
}
