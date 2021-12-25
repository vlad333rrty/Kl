package kalina.compiler.syntax.parser;

import kalina.compiler.syntax.grammar.Terminal;
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

    public Token getNextToken() {
        return scanner.getNextToken();
    }

    public Token peekNextToken() {
        return scanner.peekNextToken();
    }

    public boolean isEnd() {
        return getNextToken() == Token.endToken;
    }

    public String getTokenTag(Token token) {
        return token.getTag().name();
    }

    public boolean areEqual(Terminal terminal, Token token) {
        return terminal.getValue().equals(getTokenTag(token));
    }
}
