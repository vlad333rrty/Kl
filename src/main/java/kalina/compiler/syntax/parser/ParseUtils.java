package kalina.compiler.syntax.parser;

import java.util.List;
import java.util.stream.Collectors;

import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.operations.BoolOperation;
import kalina.compiler.expressions.operations.ComparisonOperation;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.data.ITypeDictionary;
import kalina.compiler.syntax.tokens.Token;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class ParseUtils {
    public static boolean isValidType(Token token, ITypeDictionary typeDictionary) {
        return typeDictionary.hasType(token.getValue());
    }

    public static boolean isValidDeclarationType(Token token, ITypeDictionary typeDictionary) {
        return !token.getValue().equals("void") && isValidType(token, typeDictionary);
    }

    public static boolean isArrayType(Token token) {
        return token.getValue().endsWith("[]");
    }

    public static Object getTrueValue(Token token) {
        TokenTag tag = token.getTag();
        String value = token.getValue();
        switch (tag) {
            case NUMBER_TAG:
                return Integer.parseInt(value);
            case LONG_NUMBER_TAG:
                return Long.parseLong(value.replaceAll("[lL]", "")); // l or L may appear only at the end
            case FLOAT_NUMBER_TAG:
                return Float.parseFloat(value);
            case DOUBLE_NUMBER_TAG:
                return Double.parseDouble(value);
            case BOOL_VALUE_TAG:
                return Boolean.parseBoolean(value);
            case STRING_LITERAL_TAG:
                return value;
            default:
                throw new IllegalArgumentException("Unexpected tag");
        }
    }

    public static Type convertRawType(String type) {
        switch (type) {
            case "void":
                return Type.VOID_TYPE;
            case "short":
                return Type.SHORT_TYPE;
            case "int":
                return Type.INT_TYPE;
            case "long":
                return Type.LONG_TYPE;
            case "float":
                return Type.FLOAT_TYPE;
            case "double":
                return Type.DOUBLE_TYPE;
            case "bool":
                return Type.BOOLEAN_TYPE;
            case "string":
                return Type.getType(String.class);
            default:
                return Type.getObjectType(type);
        }
    }

    public static boolean isComparisonOperation(Token token) {
        TokenTag tag = token.getTag();
        return tag == TokenTag.LESS_TAG || tag == TokenTag.LEQ_TAG || tag == TokenTag.EQUAL_TAG
                || tag == TokenTag.GREATER_TAG || tag == TokenTag.GEQ_TAG || tag == TokenTag.NOT_EQUAL_TAG;
    }

    public static ComparisonOperation getComparisonOperation(Token token) {
        TokenTag tag = token.getTag();
        if (tag == TokenTag.LESS_TAG) {
            return ComparisonOperation.LESS;
        } else if (tag == TokenTag.LEQ_TAG) {
            return ComparisonOperation.LESS_OR_EQUAL;
        } else if (tag == TokenTag.GREATER_TAG) {
            return ComparisonOperation.GREATER;
        } else if (tag == TokenTag.GEQ_TAG) {
            return ComparisonOperation.GREATER_OR_EQUAL;
        } else if (tag == TokenTag.EQUAL_TAG) {
            return ComparisonOperation.EQUAL;
        } else if (tag == TokenTag.NOT_EQUAL_TAG) {
            return ComparisonOperation.NOT_EQUAL;
        } else {
            throw new IllegalArgumentException("Unexpected token");
        }
    }

    public static boolean isBoolOperation(Token token) {
        TokenTag tag = token.getTag();
        return tag == TokenTag.BOOL_AND_TAG || tag == TokenTag.BOOL_OR_TAG || tag == TokenTag.XOR_TAG;
    }

    public static BoolOperation getBoolOperation(Token token) throws ParseException {
        TokenTag tag = token.getTag();
        switch (tag) {
            case BOOL_AND_TAG:
                return BoolOperation.AND;
            case BOOL_OR_TAG:
                return BoolOperation.OR;
            case XOR_TAG:
                return BoolOperation.XOR;
            default:
                throw new ParseException("Unknown bool operation: " + token.getValue());
        }
    }

    public static List<Type> expressionsToTypes(List<Expression> expressions) {
        return expressions.stream().map(Expression::getType).collect(Collectors.toList());
    }
}
