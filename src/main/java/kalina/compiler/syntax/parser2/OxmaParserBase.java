package kalina.compiler.syntax.parser2;

import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public abstract class OxmaParserBase {
    private final IScanner scanner;

    protected OxmaParserBase(IScanner scanner) {
        this.scanner = scanner;
    }

    protected Token getNextToken() {
        return scanner.getNextToken();
    }

    protected Token peekNextToken() {
        return scanner.peekNextToken();
    }

    protected boolean isEnd() {
        return getNextToken() == Token.endToken;
    }
}
