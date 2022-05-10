package kalina.compiler.expressions.v2.funCall;

import java.util.List;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.ValueExpression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class PrintlnExpression extends PrintExpression {
    public PrintlnExpression(List<Expression> arguments) {
        super(arguments);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        super.translateToBytecode(mv);
        printExpr(mv, new ValueExpression("\n", Type.getType(String.class)));
    }
}
