package kalina.compiler.ast.expression;

import java.util.List;

/**
 * @author vlad333rrty
 */
public class ASTStandAloneFunCallExpression extends ASTFunCallExpression {
    public ASTStandAloneFunCallExpression(String funName, List<ASTExpression> arguments) {
        super(funName, arguments);
    }
}
