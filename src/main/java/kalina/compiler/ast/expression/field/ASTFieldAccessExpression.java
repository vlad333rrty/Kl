package kalina.compiler.ast.expression.field;

import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public class ASTFieldAccessExpression implements ASTExpression {
    private final String fieldName;

    public ASTFieldAccessExpression(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
