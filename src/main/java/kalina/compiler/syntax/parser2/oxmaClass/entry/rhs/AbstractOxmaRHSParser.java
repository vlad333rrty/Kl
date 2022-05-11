package kalina.compiler.syntax.parser2.oxmaClass.entry.rhs;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public abstract class AbstractOxmaRHSParser extends OxmaParserBase {
    public AbstractOxmaRHSParser(IScanner scanner) {
        super(scanner);
    }

    public List<ASTExpression> parseRHS(int lhsSize) throws ParseException {
        List<ASTExpression> expressions = new ArrayList<>();
        ASTExpression expression = parseExpression();
        expressions.add(expression);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            ASTExpression expr = parseExpression();
            expressions.add(expr);
        }

        if (expressions.size() != lhsSize) {
            throw new ParseException("Different number of variables and values in assign");
        }
        return expressions;
    }

    protected abstract ASTExpression parseExpression() throws ParseException;
}
