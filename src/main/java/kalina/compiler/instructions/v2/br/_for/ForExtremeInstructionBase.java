package kalina.compiler.instructions.v2.br._for;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.instructions.v2.WithExpressions;

/**
 * @author vlad333rrty
 */
public abstract class ForExtremeInstructionBase extends Instruction implements WithExpressions {
    protected Instruction substituteInstruction(Instruction instruction, List<Expression> expressions) {
        return instruction instanceof WithExpressions withExpressions
                ? withExpressions.substituteExpressions(expressions)
                : instruction;
    }

    protected List<SSAVariableInfo> getVariableInfos(Instruction instruction) {
        if (instruction instanceof InitInstruction initInstruction) {
            return initInstruction.getVariableInfos();
        }
        if (instruction instanceof AbstractAssignInstruction assignInstruction) {
            return assignInstruction.getVariableInfos();
        }
        throw new IllegalArgumentException();
    }

    public abstract Optional<Instruction> getInstruction();
}
