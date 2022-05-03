package kalina.compiler.syntax.parser2.oxmaClass;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.Assert;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTableImpl;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaFieldParser;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaMethodParser;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaMethodParserBase;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaConditionExpressionsParser;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaExpressionsParser;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public class OxmaClassParser extends OxmaParserBase {
    private final OxmaMethodParserBase methodParser;
    @SuppressWarnings("unused")
    private final OxmaFieldParser fieldParser;
    private final OxmaFunctionTable functionTable;

    public OxmaClassParser(IScanner scanner) {
        super(scanner);
        this.functionTable = new OxmaFunctionTableImpl();
        OxmaExpressionsParser expressionsParser = new OxmaExpressionsParser(scanner);
        this.methodParser = new OxmaMethodParser(
                scanner,
                functionTable,
                expressionsParser,
                new OxmaConditionExpressionsParser(scanner, expressionsParser));
        this.fieldParser = new OxmaFieldParser(scanner);
    }

    public ASTClassNode parse() throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String className = token.getValue();

        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);

        ASTClassNode classNode = new ASTClassNode("", className, functionTable);
        parseClassEntry(classNode, className);

        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);

        return classNode;
    }

    private void parseClassEntry(ASTClassNode classNode, String className) throws ParseException {
        Token token = peekNextToken();
        if (token.getTag() == TokenTag.RBRACE_TAG) {
            return;
        }
        getNextToken();
        switch (token.getTag()) {
            case FUN_TAG, BEGIN_TAG -> classNode.addChild(methodParser.parse(false, className));
            case STATIC_TAG -> onStaticDetected(classNode, className);
        }
        parseClassEntry(classNode, className);
    }

    private void onStaticDetected(ASTClassNode classNode, String className) throws ParseException {
        Token token = getNextToken();
        if (token.getTag() == TokenTag.FUN_TAG) {
            classNode.addChild(methodParser.parse(true, className));
        } else {
            throw new IllegalArgumentException();
        }
    }
}
