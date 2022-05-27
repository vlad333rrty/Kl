package kalina.compiler.expressions.v2.array;

import java.util.List;

import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.cfg.data.WithIR;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.WithSubstitutableExpressions;
import kalina.compiler.utils.PrintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArrayGetElementExpression extends Expression implements
        AbstractArrayExpression,
        WithSubstitutableExpressions<Expression>,
        WithIR
{
    private final List<Expression> indices;
    private final Type elementType;
    private final Type loweredType;
    private final Type initialType;
    private final Expression variableAccessExpression;
    private final SSAVariableInfo ssaVariableInfo;

    public ArrayGetElementExpression(
            List<Expression> indices,
            Type elementType,
            Type loweredType,
            Type initialType,
            Expression variableAccessExpression,
            String varName)
    {
        this.indices = indices;
        this.elementType = elementType;
        this.loweredType = loweredType;
        this.initialType = initialType;
        this.variableAccessExpression = variableAccessExpression;
        this.ssaVariableInfo = new SSAVariableInfo(varName);
    }

    public ArrayGetElementExpression(
            List<Expression> indices,
            Type elementType,
            Type loweredType,
            Type initialType,
            Expression variableAccessExpression,
            SSAVariableInfo ssaVariableInfo)
    {
        this.indices = indices;
        this.elementType = elementType;
        this.loweredType = loweredType;
        this.initialType = initialType;
        this.variableAccessExpression = variableAccessExpression;
        this.ssaVariableInfo = ssaVariableInfo;
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        variableAccessExpression.translateToBytecode(mv);
        translateElementsAccess(mv, indices);
        mv.visitInsn(elementType.getOpcode(Opcodes.IALOAD));
    }

    @Override
    public Type getType() {
        return loweredType;
    }

    public List<Expression> getIndices() {
        return indices;
    }

    public Type getInitialType() {
        return initialType;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public String toString() {
        return getIR() + "[" + PrintUtils.listToString(indices) + "]";
    }

    @Override
    public Expression substituteExpressions(List<Expression> expressions) {
        assert expressions.size() == indices.size();
        return new ArrayGetElementExpression(
                expressions,
                elementType,
                loweredType,
                initialType,
                variableAccessExpression,
                ssaVariableInfo
        );
    }

    public ArrayGetElementExpression withCfgIndex(int cfgIndex) {
        ssaVariableInfo.setCfgIndex(cfgIndex);
        return this;
    }

    @Override
    public String getIR() {
        return ssaVariableInfo.getIR();
    }

    public String getName() {
        return ssaVariableInfo.getName();
    }
}
