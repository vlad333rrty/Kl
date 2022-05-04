package kalina.compiler.syntax.parser;

import java.util.function.Predicate;

import kalina.compiler.codegen.TypeCastOpcodesMapper;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.data.ITypeDictionary;
import kalina.compiler.syntax.tokens.Token;
import org.objectweb.asm.Type;

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

    public static void assertValidType(Token token, ITypeDictionary typeDictionary) throws ParseException {
        if (!ParseUtils.isValidType(token, typeDictionary)) {
            throw new ParseException(UNEXPECTED_TOKEN + ": type expected, got " + token.getValue());
        }
    }

    public static void assertValidDeclarationType(Token token, ITypeDictionary typeDictionary) throws ParseException {
        if (!ParseUtils.isValidDeclarationType(token, typeDictionary)) {
            throw new ParseException(UNEXPECTED_TOKEN + ": type expected, got " + token.getValue());
        }
    }

    public static void assertTypesCompatible(Type expected, Type actual) throws ParseException {
        if (!expected.equals(actual) && !TypeCastOpcodesMapper.canCast(actual, expected)) {
            throw new ParseException("Incompatible types. Cannot cast " + actual.getClassName() + " to " + expected.getClassName());
        }
    }

    public static void assertTrue(Token token, Predicate<TokenTag> predicate) throws ParseException {
        if (!predicate.test(token.getTag())) {
            throw new ParseException(UNEXPECTED_TOKEN + ": "+ token.getValue());
        }
    }
}
