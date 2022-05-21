package kalina.compiler.syntax.parser2;

import kalina.compiler.expressions.operations.BoolOperation;
import kalina.compiler.expressions.operations.ComparisonOperation;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.tokens.Token;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class ParseUtils {

    public static Object getTrueValue(Token token) {
        TokenTag tag = token.getTag();
        String value = token.getValue();
        return switch (tag) {
            case NUMBER_TAG -> Integer.parseInt(value);
            case LONG_NUMBER_TAG -> Long.parseLong(value.replaceAll("[lL]", "")); // l or L may appear only at the end
            case FLOAT_NUMBER_TAG -> Float.parseFloat(value);
            case DOUBLE_NUMBER_TAG -> Double.parseDouble(value);
            case BOOL_VALUE_TAG -> Boolean.parseBoolean(value);
            case STRING_LITERAL_TAG -> value;
            default -> throw new IllegalArgumentException("Unexpected tag");
        };
    }

    private static Type convertRawType(String type) {
        return switch (type) {
            case "void" -> Type.VOID_TYPE;
            case "short" -> Type.SHORT_TYPE;
            case "int" -> Type.INT_TYPE;
            case "long" -> Type.LONG_TYPE;
            case "float" -> Type.FLOAT_TYPE;
            case "double" -> Type.DOUBLE_TYPE;
            case "bool" -> Type.BOOLEAN_TYPE;
            case "string" -> Type.getType(String.class);
            default -> Type.getObjectType(type);
        };
    }

    public static Type convertRawType(Token token) {
        if (token.getTag() == TokenTag.ARRAY_TYPE_TAG) {
            return Type.getType(buildDescriptorForArray(token.getValue()));
        } else {
            return convertRawType(token.getValue());
        }
    }

    private static String buildDescriptorForArray(String value) {
        String arrayType = value.substring(0, value.indexOf("["));
        String typeDescriptor = convertRawType(arrayType).getDescriptor();
        int dimension = (int)value.chars().filter(c -> c == '[').count();
        return "[".repeat(dimension).concat(typeDescriptor);
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
        return switch (tag) {
            case BOOL_AND_TAG -> BoolOperation.AND;
            case BOOL_OR_TAG -> BoolOperation.OR;
            case XOR_TAG -> BoolOperation.XOR;
            default -> throw new ParseException("Unknown bool operation: " + token.getValue());
        };
    }

    public static boolean isPrimitiveType(TokenTag tag) {
        return tag == TokenTag.SHORT_TAG || tag == TokenTag.INT_TAG || tag == TokenTag.LONG_TAG
                || tag == TokenTag.FLOAT_TAG || tag == TokenTag.DOUBLE_TAG || tag == TokenTag.STRING_TAG
                || tag == TokenTag.BOOL_TAG;
    }
}
