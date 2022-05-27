package kalina.compiler.instructions.v2;

import java.util.List;
import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.ClassPropertyCallExpression;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class ClassPropertyCallChainInstruction extends Instruction implements WithExpressions {
    private final ClassPropertyCallExpression propertyCallExpression;

    public ClassPropertyCallChainInstruction(ClassPropertyCallExpression propertyCallExpression) {
        this.propertyCallExpression = propertyCallExpression;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            propertyCallExpression.translateToBytecode(mv.get());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public List<Expression> getExpressions() {
        return propertyCallExpression.getExpressions();
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        return new ClassPropertyCallChainInstruction(propertyCallExpression.substituteExpressions(expressions));
    }

    @Override
    public String toString() {
        return propertyCallExpression.toString();
    }
}
