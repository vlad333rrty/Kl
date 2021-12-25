package kalina.compiler.syntax.parser;

import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.data.ITypeDictionary;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
class Assert {
    private static final String UNEXPECTED_TOKEN = "Unexpected token";

    static void assertTag(Token token, TokenTag tag) {
        if (token.getTag() != tag) {
            throw new IllegalStateException(UNEXPECTED_TOKEN + " : "+ token.getTag());
        }
    }

    static void assertValidType(Token token, ITypeDictionary typeDictionary) {
        if (!ParseUtils.isValidType(token, typeDictionary)) {
            throw new IllegalStateException(UNEXPECTED_TOKEN + ": type expected, got " + token.getValue());
        }
    }
}
