package kalina.compiler.ast.expression.field;

/**
 * @author vlad333rrty
 */
public class ASTOtherFieldAccessExpression extends ASTFieldAccessExpression {
    private final String ownerName;

    public ASTOtherFieldAccessExpression(String fieldName, String ownerName) {
        super(fieldName);
        this.ownerName = ownerName;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
