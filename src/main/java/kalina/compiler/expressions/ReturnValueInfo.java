package kalina.compiler.expressions;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ReturnValueInfo {
    private final Type returnType;
    private final Expression returnValue;

    public ReturnValueInfo(Type returnType, Expression returnValue) {
        this.returnType = returnType;
        this.returnValue = returnValue;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Expression getReturnValue() {
        return returnValue;
    }
}
