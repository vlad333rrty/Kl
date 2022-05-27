package kalina.compiler.expressions.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ClassPropertyCallExpression extends Expression implements WithSubstitutableExpressions<Expression> {
    private final List<Expression> expressions;

    public ClassPropertyCallExpression(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        for (Expression expression : expressions) {
            expression.translateToBytecode(mv);
        }
    }

    @Override
    public Type getType() {
        return expressions.get(expressions.size() - 1).getType();
    }

    @Override
    public String toString() {
        return expressions.stream().map(Objects::toString).collect(Collectors.joining("."));
    }

    public List<Expression> getExpressions() {
        return expressions.stream().findFirst().stream().toList();
    }

    @Override
    public ClassPropertyCallExpression substituteExpressions(List<Expression> expressions) {
        if (expressions.size() > 1) {
            throw new IllegalArgumentException();
        }
        List<Expression> newExpressions = new ArrayList<>(this.expressions);
        expressions.stream().findFirst().map(x -> newExpressions.set(0, x));
        return new ClassPropertyCallExpression(newExpressions);
    }
}
