package kalina.compiler.cfg.optimizations.dce;

import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.operations.ArithmeticOperation;
import kalina.compiler.expressions.v2.ClassPropertyCallExpression;
import kalina.compiler.expressions.v2.array.ArrayGetElementExpression;
import kalina.compiler.expressions.v2.funCall.FunCallExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.instructions.v2.assign.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.assign.ArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.assign.FieldArrayElementAssignInstruction;

/**
 * @author vlad333rrty
 */
public class DangerousInstructionsDetector {
    public static boolean isDangerousInstruction(Instruction instruction) {
        if (instruction instanceof AbstractAssignInstruction assignInstruction) {
            if (instruction instanceof ArrayElementAssignInstruction || instruction instanceof FieldArrayElementAssignInstruction) {
                return true;
            }
            return assignInstruction.getRHS().stream().anyMatch(DangerousInstructionsDetector::isDangerousExpression);
        }
        if (instruction instanceof InitInstruction initInstruction) {
            return initInstruction.getRHS().stream().anyMatch(DangerousInstructionsDetector::isDangerousExpression);
        }
        return false;
    }

    private static boolean isDangerousExpression(Expression expression) {
        if (expression instanceof ArrayGetElementExpression) {
            return true;
        }
        if (expression instanceof ArithmeticExpression arithmeticExpression) {
            return arithmeticExpression.getTerms().stream().anyMatch(DangerousInstructionsDetector::withDangerousDivide)
                    || arithmeticExpression.getTerms().stream().anyMatch(term -> term.getFactors().stream().map(Factor::getExpression).anyMatch(DangerousInstructionsDetector::isDangerousExpression));
        }
        return expression instanceof FunCallExpression || expression instanceof ClassPropertyCallExpression;
    }

    private static boolean withDangerousDivide(Term term) {
        for (int i = 0; i < term.getOperations().size(); i++) {
            ArithmeticOperation operation = term.getOperations().get(i);
            if (operation == ArithmeticOperation.DIVIDE && !(term.getFactors().get(i + 1).getExpression() instanceof ValueExpression)) {
                return true;
            }
        }
        return false;
    }
}
