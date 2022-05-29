package kalina.compiler.cfg.optimizations;

import java.util.Iterator;

import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.operations.ArithmeticOperation;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ConstantExpressionDetector {
    public static boolean isNumberConstant(Expression expression) {
        if (expression instanceof ValueExpression valueExpression) {
            return isNumberConstant(valueExpression);
        }
        if (expression instanceof ArithmeticExpression arithmeticExpression) {
            return parseAr(arithmeticExpression);
        }
        return false;
    }

    private static boolean parseAr(ArithmeticExpression arithmeticExpression) {
        Iterator<Term> termIterator = arithmeticExpression.getTerms().iterator();
        if (!termIterator.hasNext()) {
            throw new IllegalArgumentException("Unexpected empty terms list");
        }
        Iterator<ArithmeticOperation> iterator = arithmeticExpression.getOperations().iterator();
        boolean res = parseT(termIterator.next());
        while (iterator.hasNext()) {
            iterator.next();
            res = res && parseT(termIterator.next());
        }

        return res;
    }

    private static boolean parseT(Term term) {
        Iterator<Factor> factorIterator = term.getFactors().iterator();
        if (!factorIterator.hasNext()) {
            throw new IllegalArgumentException("Unexpected empty factors list");
        }
        Iterator<ArithmeticOperation> iterator = term.getOperations().iterator();
        boolean res = parseF(factorIterator.next());
        while (iterator.hasNext()) {
            iterator.next();
            res = res && parseF(factorIterator.next());
        }

        return res;
    }

    private static boolean parseF(Factor factor) {
        if (factor.getExpression() instanceof ArithmeticExpression arithmeticExpression) {
            return parseAr(arithmeticExpression);
        }
        if (factor.getExpression() instanceof ValueExpression valueExpression) {
            return isNumberConstant(valueExpression);
        }
        return false;
    }

    private static boolean isNumberConstant(ValueExpression valueExpression) {
        Type type = valueExpression.getType();
        int sort = type.getSort();
        return switch (sort) {
            case Type.INT, Type.LONG, Type.FLOAT, Type.DOUBLE -> true;
            default -> false;
        };
    }
}
