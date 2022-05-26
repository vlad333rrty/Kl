package kalina.compiler.instructions.v2;

import kalina.compiler.expressions.CondExpression;
import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public interface WithCondition {
    CondExpression getCondExpression();
    Instruction substituteCondExpression(CondExpression expression);
}
