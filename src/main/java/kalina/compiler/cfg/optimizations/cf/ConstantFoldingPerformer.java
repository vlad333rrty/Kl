package kalina.compiler.cfg.optimizations.cf;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithRHS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class ConstantFoldingPerformer {
    private static final Logger logger = LogManager.getLogger(ConstantFoldingPerformer.class);

    public void perform(ControlFlowGraph controlFlowGraph)  {
        for (AbstractCFGNode node : controlFlowGraph.nodes()) {
            List<Instruction> transformedInstructions = new ArrayList<>();
            for (Instruction instruction : node.getBasicBlock().getInstructions()) {
                if (instruction instanceof WithRHS withRHS) {
                    List<Expression> transformedExpressions = withRHS.getRHS().stream()
                            .map(CfArithmeticExpressionParser::parseExpression)
                            .toList();
                    transformedInstructions.add(withRHS.substituteExpressions(transformedExpressions));
                } else {
                    transformedInstructions.add(instruction);
                }
            }
            node.getBasicBlock().setInstructions(transformedInstructions);
        }
    }
}
