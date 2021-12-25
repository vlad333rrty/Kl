package kalina.compiler.expressions;

import kalina.compiler.codegen.ExpressionCodeGen;
import kalina.compiler.codegen.IExpressionCodeGen;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public abstract class Expression {
    protected IExpressionCodeGen expressionCodeGen = new ExpressionCodeGen();

    // after a call leaves a value on the stack
    public abstract void translateToBytecode(MethodVisitor mv);

    /**
     *
     * @return Type of the value left on the stack by the expression
     */
    public abstract Type getType();
}
