package kalina.compiler.syntax.scanner;

import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public interface IScanner {
    Token getNextToken();

    Token peekNextToken();
}
