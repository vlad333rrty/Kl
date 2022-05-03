package kalina.compiler.instructions.v2;

import java.util.List;
import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.syntax.parser.data.VariableInfo;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class AssignInstruction extends Instruction {
    private final List<VariableInfo> lhs;
    private final List<Expression> rhs;

    public AssignInstruction(List<VariableInfo> lhs, List<Expression> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            for (int i = 0, rhsSize = rhs.size(); i < rhsSize; i++) {
                Expression expression = rhs.get(i);
                expression.translateToBytecode(methodVisitor);
                Type type = lhs.get(i).getType();
                Type expressionType = expression.getType();
                if (!type.equals(expressionType)) {
                    expressionCodeGen.cast(expressionType, type, methodVisitor);
                }
            }
            for (int i = lhs.size() - 1; i >= 0; i--) {
                VariableInfo variableInfo = lhs.get(i);
                methodVisitor.visitVarInsn(variableInfo.getType().getOpcode(Opcodes.ISTORE), variableInfo.getIndex());
            }
        } else {
            throw new IllegalArgumentException("Either mv or cw should be present");
        }
    }
}
