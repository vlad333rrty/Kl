package kalina.compiler.cfg.optimizations.cf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.optimizations.ConstantExpressionDetector;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.operations.ArithmeticOperation;
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
                    List<Expression> transformedExpressions = withRHS.getRHS().stream().map(this::parseExpression).toList();
                    transformedInstructions.add(withRHS.substituteExpressions(transformedExpressions));
                } else {
                    transformedInstructions.add(instruction);
                }
            }
            node.getBasicBlock().setInstructions(transformedInstructions);
        }
    }

    private Expression parseExpression(Expression expression) {
        if (expression instanceof ValueExpression) {
            return expression;
        }
        if (expression instanceof ArithmeticExpression arithmeticExpression) {
            if (ConstantExpressionDetector.isNumberConstant(arithmeticExpression)) {
                try {
                    Number number = NumberCalculator.parseAr(arithmeticExpression);
                    return new ValueExpression(number, arithmeticExpression.getType());
                } catch (ArithmeticException | IncompatibleTypesException e) {
                    logger.warn(e);
                }
            }
            return arithmeticExpression;
        }
        return expression;
    }

    private long parseAr(ArithmeticExpression arithmeticExpression) {
        Iterator<Term> termIterator = arithmeticExpression.getTerms().iterator();
        if (!termIterator.hasNext()) {
            throw new IllegalArgumentException("Unexpected empty factors list");
        }
        Iterator<ArithmeticOperation> iterator = arithmeticExpression.getOperations().iterator();
        long res = parseT(termIterator.next());
        while (iterator.hasNext()) {
            ArithmeticOperation operation = iterator.next();
            long next = parseT(termIterator.next());
            if (operation == ArithmeticOperation.PLUS) {
                res += next;
            } else {
                res -= next;
            }
        }

        return res;
    }

    private long parseT(Term term) {
        Iterator<Factor> factorIterator = term.getFactors().iterator();
        if (!factorIterator.hasNext()) {
            throw new IllegalArgumentException("Unexpected empty factors list");
        }
        Iterator<ArithmeticOperation> iterator = term.getOperations().iterator();
        long res = parseF(factorIterator.next());
        while (iterator.hasNext()) {
            ArithmeticOperation operation = iterator.next();
            long next = parseF(factorIterator.next());
            if (operation == ArithmeticOperation.MULTIPLY) {
                res *= next;
            } else {
                if (next == 0) {
                    throw new IllegalArgumentException("Division by zero!");
                }
                res /= next;
            }
        }

        return res;
    }

    private long parseF(Factor factor) {
        if (factor.getExpression() instanceof ArithmeticExpression arithmeticExpression) {
            return parseAr(arithmeticExpression);
        }
        if (factor.getExpression() instanceof ValueExpression valueExpression) {
            return ((Number)valueExpression.getValue()).longValue();
        }
        throw new IllegalArgumentException();
    }
}
