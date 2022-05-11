package kalina.compiler.syntax.parser2.oxmaClass;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser2.Assert;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTableImpl;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaFieldParser;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaMethodParser;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaMethodParserBase;
import kalina.compiler.syntax.parser2.oxmaClass.entry.rhs.OxmaRHSParser;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaConditionExpressionsParser;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaExpressionsParser;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public class OxmaClassParser extends OxmaParserBase {
    private final OxmaMethodParserBase methodParser;
    @SuppressWarnings("all")
    private final OxmaFieldParser fieldParser;

    public OxmaClassParser(IScanner scanner) {
        super(scanner);
        OxmaExpressionsParser expressionsParser = new OxmaExpressionsParser(scanner);
        this.methodParser = new OxmaMethodParser(
                scanner,
                expressionsParser,
                new OxmaConditionExpressionsParser(scanner, expressionsParser),
                new OxmaRHSParser(scanner, expressionsParser));
        this.fieldParser = new OxmaFieldParser(scanner);
    }

    public ASTClassNode parse() throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String className = token.getValue();
        final String parentClassName;
        if (peekNextToken().getTag() == TokenTag.EXTENDS_TAG) {
            getNextToken(); // skip `extends`
            Token superClass = getNextToken();
            Assert.assertTag(superClass, TokenTag.IDENT_TAG);
            parentClassName = superClass.getValue();
        } else {
            parentClassName = "java/lang/Object";
        }

        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);

        OxmaFunctionTable functionTable = new OxmaFunctionTableImpl();
        ASTClassNode classNode = new ASTClassNode(parentClassName, className, functionTable);
        parseClassEntry(classNode, className, functionTable);

        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);

        return classNode;
    }

    private void parseClassEntry(ASTClassNode classNode, String className, OxmaFunctionTable functionTable) throws ParseException {
        Token token = peekNextToken();
        if (token.getTag() == TokenTag.RBRACE_TAG) {
            return;
        }
        getNextToken();
        switch (token.getTag()) {
            case BEGIN_TAG -> classNode.addChild(methodParser.parseBegin(className));
            case FUN_TAG -> classNode.addChild(methodParser.parse(false, className, functionTable));
            case STATIC_TAG -> onStaticDetected(classNode, className, functionTable);
        }
        parseClassEntry(classNode, className, functionTable);
    }

    private void onStaticDetected(ASTClassNode classNode, String className, OxmaFunctionTable functionTable) throws ParseException {
        Token token = getNextToken();
        if (token.getTag() == TokenTag.FUN_TAG) {
            classNode.addChild(methodParser.parse(true, className, functionTable));
        } else {
            throw new IllegalArgumentException();
        }
    }
}
