package kalina.compiler.syntax.parser2.oxmaClass.entry;

import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.ASTFieldNode;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser2.Assert;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.ParseUtils;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaExpressionsParser;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class OxmaFieldParser extends OxmaParserBase {
    private static final Logger logger = LogManager.getLogger(OxmaFieldParser.class);

    private final OxmaExpressionsParser expressionsParser;

    public OxmaFieldParser(IScanner scanner, OxmaExpressionsParser expressionsParser) {
        super(scanner);
        this.expressionsParser = expressionsParser;
    }

    public ASTFieldNode parse(ClassEntryUtils.AccessModifier accessModifier, List<ClassEntryUtils.Modifier> modifiers) throws ParseException {
        Token token = getNextToken();
        switch (token.getTag()) {
            case SHORT_TAG, INT_TAG, LONG_TAG, FLOAT_TAG, DOUBLE_TAG, STRING_TAG, BOOL_TAG, ARRAY_TYPE_TAG, IDENT_TAG -> {
                return parseWithType(accessModifier, modifiers, token);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public ASTFieldNode parseWithType(
            ClassEntryUtils.AccessModifier accessModifier,
            List<ClassEntryUtils.Modifier> modifiers,
            Token typeToken) throws ParseException
    {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String varName = token.getValue();
        Optional<ASTExpression> rhs = parseRHS();
        return new ASTFieldNode(varName, ParseUtils.convertRawType(typeToken), rhs, accessModifier, modifiers);
    }

    private Optional<ASTExpression> parseRHS() throws ParseException {
        if (peekNextToken().getTag() == TokenTag.ASSIGN_TAG) {
            getNextToken();
            return Optional.of(expressionsParser.parse());
        } else {
            return Optional.empty();
        }
    }
}
