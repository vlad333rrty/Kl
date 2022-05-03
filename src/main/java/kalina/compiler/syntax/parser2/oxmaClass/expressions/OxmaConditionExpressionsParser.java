package kalina.compiler.syntax.parser2.oxmaClass.expressions;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.ast.expression.ASTCondExpression;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.expressions.operations.ComparisonOperation;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser.ParseUtils;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public class OxmaConditionExpressionsParser extends AbstractOxmaExpressionsParser {
    private final OxmaExpressionsParser expressionsParser;

    public OxmaConditionExpressionsParser(IScanner scanner, OxmaExpressionsParser expressionsParser) {
        super(scanner);
        this.expressionsParser = expressionsParser;
    }

    // todo improve logic
    @Override
    public ASTCondExpression parse() throws ParseException {
        ASTExpression expression = expressionsParser.parse();
        List<ASTExpression> expressions = new ArrayList<>();
        expressions.add(expression);
        List<ComparisonOperation> operations = new ArrayList<>();
        if (ParseUtils.isComparisonOperation(peekNextToken())) {
            operations.add(ParseUtils.getComparisonOperation(getNextToken()));
            expressions.add(expressionsParser.parse());
        }

        return new ASTCondExpression(expressions, operations);
    }
}
