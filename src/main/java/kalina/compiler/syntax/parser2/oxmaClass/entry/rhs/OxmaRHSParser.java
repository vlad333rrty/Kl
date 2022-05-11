package kalina.compiler.syntax.parser2.oxmaClass.entry.rhs;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaExpressionsParser;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public class OxmaRHSParser extends AbstractOxmaRHSParser {
    private final OxmaExpressionsParser expressionsParser;

    public OxmaRHSParser(IScanner scanner, OxmaExpressionsParser expressionsParser) {
        super(scanner);
        this.expressionsParser = expressionsParser;
    }

    @Override
    protected ASTExpression parseExpression() throws ParseException {
        return expressionsParser.parse();
    }
}
