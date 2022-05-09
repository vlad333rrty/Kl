package kalina.compiler.instructions;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.ExpressionCodeGen;
import kalina.compiler.codegen.IExpressionCodeGen;
import kalina.compiler.codegen.typeCast.TypeCaster;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public abstract class Instruction {
    protected IExpressionCodeGen expressionCodeGen = new ExpressionCodeGen(new TypeCaster());

    public abstract void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException;
}
