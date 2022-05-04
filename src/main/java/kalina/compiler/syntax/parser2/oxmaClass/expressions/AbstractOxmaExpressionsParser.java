package kalina.compiler.syntax.parser2.oxmaClass.expressions;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public abstract class AbstractOxmaExpressionsParser extends OxmaParserBase {
    protected AbstractOxmaExpressionsParser(IScanner scanner) {
        super(scanner);
    }

    public abstract ASTExpression parse() throws ParseException;
}
