package kalina.compiler.ast.expression;

import java.util.List;

import kalina.compiler.utils.PrintUtils;

/**
 * @author vlad333rrty
 */
public interface ASTExpression {

    default String complexExpressionToString(List<?> elements, List<?> operations) {
        return PrintUtils.complexExpressionToString(elements, operations);
    }
}
