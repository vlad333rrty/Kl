package kalina.compiler.syntax.parser;

import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public abstract class AbstractParser implements IParser {
    private final IScanner scanner;

    protected AbstractParser(IScanner scanner) {
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
