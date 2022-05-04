package kalina.compiler.syntax.parser2;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser2.oxmaClass.OxmaClassParser;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class OxmaParser extends OxmaParserBase {
    private static final Logger logger = LogManager.getLogger(OxmaParser.class);

    private final OxmaClassParser classParser;

    public OxmaParser(IScanner scanner) {
        super(scanner);
        this.classParser = new OxmaClassParser(scanner);
    }

    public ASTRootNode parse() throws ParseException {
       ASTRootNode root = new ASTRootNode();
       parseInt(root);
       return root;
    }

    private void parseInt(ASTRootNode root) throws ParseException {
        Token token = getNextToken();
        if (token.getTag() == TokenTag.CLASS_TAG) {
            ASTClassNode classNode = classParser.parse();
            root.addChild(classNode);
            parseInt(root);
        }
        if (isEnd()) {
            logger.info("Reached EOF. Parsing is completed.");
        } else {
            throw new ParseException("Unexpected token: " + token.getTag());
        }
    }
}
