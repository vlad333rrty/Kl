package kalina.compiler.instructions.v2;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.data.WithIR;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class PhiFunInstruction extends Instruction {
    private final List<VariableExpression> arguments;
    private final String lhsIR;

    public PhiFunInstruction(List<VariableExpression> arguments, String lhsIR) {
        this.arguments = arguments;
        this.lhsIR = lhsIR;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        throw new UnsupportedOperationException();
    }

    public List<VariableExpression> getArguments() {
        return arguments;
    }

    public WithIR getLhsIR() {
        return new WithIR() {
            @Override
            public String getIR() {
                return lhsIR;
            }
        };
    }
}
