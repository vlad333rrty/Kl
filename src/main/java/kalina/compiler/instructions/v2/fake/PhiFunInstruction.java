package kalina.compiler.instructions.v2.fake;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.data.WithIR;
import kalina.compiler.codegen.CodeGenException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class PhiFunInstruction extends FakeInstruction {
    private final List<PhiArgumentExpression> arguments;
    private final String lhsIR;

    public PhiFunInstruction(List<PhiArgumentExpression> arguments, String lhsIR) {
        this.arguments = arguments;
        this.lhsIR = lhsIR;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        throw new UnsupportedOperationException();
    }

    public List<PhiArgumentExpression> getArguments() {
        return arguments;
    }

    public WithIR getLhsIR() {
        return () -> lhsIR;
    }

    @Override
    public String toString() {
        return lhsIR + " = φ(" + arguments.toString() + ")";
    }
}
