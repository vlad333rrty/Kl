package kalina.compiler.instructions.v2;

import java.util.List;
import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class FunCallInstruction extends Instruction implements WithExpressions {
    private final AbstractFunCallExpression expression;

    public FunCallInstruction(AbstractFunCallExpression expression) {
        this.expression = expression;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            expression.translateToBytecode(mv.get());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return expression.toString();
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public List<Expression> getExpressions() {
        return expression.getArguments();
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        return new FunCallInstruction(expression.substituteArguments(expressions));
    }
}
