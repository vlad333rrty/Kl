package kalina.compiler.expressions.v2;

import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.cfg.data.WithIR;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArrayElementAssignExpression extends Expression implements WithIR {
    private final VariableInfo variableInfo;

    public ArrayElementAssignExpression(VariableInfo variableInfo) {
        this.variableInfo = variableInfo;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {

    }

    @Override
    public Type getType() {
        return variableInfo.getArrayVariableInfoOrElseThrow().getLoweredType();
    }

    @Override
    public String getIR() {
        return variableInfo.getIR();
    }
}
