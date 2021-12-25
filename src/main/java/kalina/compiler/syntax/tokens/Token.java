package kalina.compiler.syntax.tokens;

import kalina.compiler.syntax.build.TokenTag;

/**
 * @author vlad333rrty
 */
public class Token {
    public static final Token endToken = new Token(TokenTag.END_TAG, "END_TAG");

    private final TokenTag tag;
    private final String value;

    public Token(TokenTag tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    public TokenTag getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token{" +
                "tag=" + tag +
                ", value='" + value + '\'' +
                '}';
    }
}
