package kalina.compiler.instructions.v2.br._for;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithExpressions;
import kalina.compiler.instructions.v2.WithLHS;
import kalina.compiler.instructions.v2.WithRHS;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class ForDeclarationInstruction extends ForExtremeInstructionBase implements WithLHS {
    private final Optional<Instruction> declarations;

    public ForDeclarationInstruction(Optional<Instruction> declarations) {
        this.declarations = declarations;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (mv.isPresent()) {
            MethodVisitor methodVisitor = mv.get();
            if (declarations.isPresent()) {
                Instruction instruction = declarations.get();
                instruction.translateToBytecode(mv, cw);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public List<Expression> getExpressions() {
        if (declarations.isEmpty()) {
            return List.of();
        }
        if (declarations.get() instanceof WithExpressions withExpressions) {
            return withExpressions.getExpressions();
        }
        return List.of();
    }

    @Override
    public Instruction substituteExpressions(List<Expression> expressions) {
        if (declarations.isEmpty()) {
            return this;
        }
        Instruction instruction = substituteInstruction(declarations.get(), expressions);
        return new ForDeclarationInstruction(Optional.of(instruction));
    }

    @Override
    public String toString() {
        return declarations.isPresent() ? declarations.get().toString() : "";
    }

    public Optional<Instruction> getDeclarations() {
        return declarations;
    }

    @Override
    public List<SSAVariableInfo> getVariableInfos() {
        if (declarations.isEmpty()) {
            return List.of();
        }
        return getVariableInfos(declarations.get());
    }

    @Override
    public Optional<Instruction> getInstruction() {
        return declarations;
    }

    @Override
    public List<Expression> getRHS() {
        if (declarations.isEmpty()) {
            return List.of();
        }
        if (declarations.get() instanceof WithRHS withRHS) {
            return withRHS.getRHS();
        }
        return List.of();
    }
}
