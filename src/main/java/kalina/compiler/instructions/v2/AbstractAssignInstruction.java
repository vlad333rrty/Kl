package kalina.compiler.instructions.v2;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public abstract class AbstractAssignInstruction extends Instruction implements WithExpressions, WithRHS {
    private final List<VariableInfo> lhs;
    private final List<Expression> rhs;

    public AbstractAssignInstruction(List<VariableInfo> lhs, List<Expression> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            for (int i = 0, rhsSize = rhs.size(); i < rhsSize; i++) {
                Expression expression = rhs.get(i);
                visitBeforeRHS(methodVisitor, lhs.get(i));
                expression.translateToBytecode(methodVisitor);
            }
            for (int i = lhs.size() - 1; i >= 0; i--) {
                VariableInfo variableInfo = lhs.get(i);
                visitStore(methodVisitor, variableInfo);
            }
        } else {
            throw new IllegalArgumentException("Either mv or cw should be present");
        }
    }

    public List<VariableInfo> getLhs() {
        return lhs;
    }

    public List<Expression> getRhs() {
        return rhs;
    }

    protected abstract void visitBeforeRHS(MethodVisitor mv, VariableInfo variableInfo) throws CodeGenException;

    protected abstract void visitStore(MethodVisitor mv, VariableInfo variableInfo);

    public abstract AbstractAssignInstruction withRHS(List<Expression> rhs);

    @Override
    public List<Expression> getExpressions() {
        return rhs;
    }

    @Override
    public List<Expression> getRHS() {
        return rhs;
    }
}
