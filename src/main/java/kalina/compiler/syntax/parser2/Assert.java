package kalina.compiler.syntax.parser2;

import java.util.function.Predicate;

import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public class Assert {
    private static final String UNEXPECTED_TOKEN = "Unexpected token";

    public static void assertTag(Token token, TokenTag tag) throws ParseException {
        if (token.getTag() != tag) {
            throw new ParseException(UNEXPECTED_TOKEN + ": "+ token.getValue());
        }
    }

    public static void assertTrue(Token token, Predicate<TokenTag> predicate) throws ParseException {
        if (!predicate.test(token.getTag())) {
            throw new ParseException(UNEXPECTED_TOKEN + ": "+ token.getValue());
        }
    }
}
