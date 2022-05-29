package kalina.compiler.instructions.v2;

import java.util.List;

import kalina.compiler.expressions.Expression;

/**
 * @author vlad333rrty
 */
public interface WithRHS extends InstructionWithSubstitutableExpressions {
    List<Expression> getRHS();
}
