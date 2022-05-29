package kalina.compiler.cfg.optimizations.cf;

import java.util.Iterator;

import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.cfg.validator.TypesComparator;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.operations.ArithmeticOperation;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
class NumberCalculator {
    private static Type currentType = Type.INT_TYPE;

    public static Number parseAr(ArithmeticExpression arithmeticExpression) throws IncompatibleTypesException {
        Iterator<Term> termIterator = arithmeticExpression.getTerms().iterator();
        if (!termIterator.hasNext()) {
            throw new IllegalArgumentException("Unexpected empty factors list");
        }
        Iterator<ArithmeticOperation> iterator = arithmeticExpression.getOperations().iterator();
        Number res = parseT(termIterator.next());
        while (iterator.hasNext()) {
            ArithmeticOperation operation = iterator.next();
            Number next = parseT(termIterator.next());
            res = performCalculation(res, next, operation);
        }

        return res;
    }

    private static Number parseT(Term term) throws IncompatibleTypesException {
        Iterator<Factor> factorIterator = term.getFactors().iterator();
        if (!factorIterator.hasNext()) {
            throw new IllegalArgumentException("Unexpected empty factors list");
        }
        Iterator<ArithmeticOperation> iterator = term.getOperations().iterator();
        Number res = parseF(factorIterator.next());
        while (iterator.hasNext()) {
            ArithmeticOperation operation = iterator.next();
            Number next = parseF(factorIterator.next());
            if (operation == ArithmeticOperation.DIVIDE && next.doubleValue() == 0.) {
                throw new IllegalArgumentException("Division by zero!");
            }
            res = performCalculation(res, next, operation);
        }

        return res;
    }

    private static Number parseF(Factor factor) throws IncompatibleTypesException {
        final Number number;
        if (factor.getExpression() instanceof ArithmeticExpression arithmeticExpression) {
            number = parseAr(arithmeticExpression);
            currentType = TypesComparator.getMax(currentType, arithmeticExpression.getType());
        } else if (factor.getExpression() instanceof ValueExpression valueExpression) {
            number = (Number)valueExpression.getValue();
            currentType = TypesComparator.getMax(currentType, valueExpression.getType());
        } else {
            throw new IllegalArgumentException();
        }
        return factor.shouldNegate() ? performCalculation(-1, number, ArithmeticOperation.MULTIPLY) : number;
    }

    private static Number performCalculation(Number left, Number right, ArithmeticOperation operation) {
        try {
            switch (currentType.getSort()) {
                case Type.INT -> {
                    int leftVal = left.intValue();
                    int rightVal = right.intValue();
                    return switch (operation) {
                        case PLUS -> Math.addExact(leftVal, rightVal);
                        case MINUS -> Math.subtractExact(leftVal, rightVal);
                        case MULTIPLY -> Math.multiplyExact(leftVal, rightVal);
                        case DIVIDE -> leftVal / rightVal;
                    };
                }
                case Type.LONG -> {
                    long leftVal = left.longValue();
                    long rightVal = right.longValue();
                    return switch (operation) {
                        case PLUS -> Math.addExact(leftVal, rightVal);
                        case MINUS -> Math.subtractExact(leftVal, rightVal);
                        case MULTIPLY -> Math.multiplyExact(leftVal, rightVal);
                        case DIVIDE -> leftVal / rightVal;
                    };
                }
                case Type.FLOAT -> {
                    float leftVal = left.floatValue();
                    float rightVal = right.floatValue();
                    return switch (operation) {
                        case PLUS -> leftVal + rightVal;
                        case MINUS -> leftVal - rightVal;
                        case MULTIPLY -> leftVal * rightVal;
                        case DIVIDE -> leftVal / rightVal;
                    };
                }
                case Type.DOUBLE -> {
                    double leftVal = left.doubleValue();
                    double rightVal = right.doubleValue();
                    return switch (operation) {
                        case PLUS -> leftVal + rightVal;
                        case MINUS -> leftVal - rightVal;
                        case MULTIPLY -> leftVal * rightVal;
                        case DIVIDE -> leftVal / rightVal;
                    };
                }
                default -> throw new IllegalArgumentException();
            }
        } catch (ArithmeticException e) {
            throw new ArithmeticException("Value is too big to be stored as " + currentType.getClassName());
        }
    }
}