package kalina.compiler.expressions;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.ExpressionCodeGen;
import kalina.compiler.codegen.IExpressionCodeGen;
import kalina.compiler.codegen.TypeCaster;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public abstract class Expression {
    protected IExpressionCodeGen expressionCodeGen = new ExpressionCodeGen(new TypeCaster());

    // after a call leaves a value on the stack
    public abstract void translateToBytecode(MethodVisitor mv) throws CodeGenException;

    /**
     *
     * @return Type of the value left on the stack by the expression
     */
    public abstract Type getType();
}
