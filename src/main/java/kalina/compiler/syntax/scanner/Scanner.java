package kalina.compiler.syntax.scanner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.tokens.Token;

public class Scanner implements IScanner {
    private final java.util.Scanner scanner;
    private Token peekedToken;

    public Scanner(String fileName) throws IOException {
        scanner = new java.util.Scanner(new FileInputStream(fileName));
    }

    @Override
    public Token getNextToken() {
        if (peekedToken != null) {
            Token t = peekedToken;
            peekedToken = null;
            return t;
        }
        if (!scanner.hasNext()) {
            return Token.endToken;
        }

        int tag = scanner.nextInt();
        String value;
        if (tag == TokenTag.STRING_LITERAL_TAG.ordinal()) {
            value = removeQuotes(scanner.nextLine().trim());
        } else {
            value = scanner.next();
        }

        return new Token(Arrays.stream(TokenTag.values()).filter(t -> t.ordinal() == tag)
                .findFirst().orElseThrow(), value);
    }

    @Override
    public Token peekNextToken() {
        if (peekedToken != null) {
            return peekedToken;
        }
        peekedToken = getNextToken();
        return peekedToken;
    }

    private String removeQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }
}