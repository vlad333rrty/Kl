package kalina.compiler.instructions.v2;

import java.util.List;

import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public interface InstructionWithSubstitutableExpressions {
    Instruction substituteExpressions(List<Expression> expressions);
}
