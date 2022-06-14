package kalina.compiler.syntax.scanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public class Scanner2 implements IScanner {
    private final List<Token> tokens;
    private int index;

    public Scanner2(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static Scanner2 fromLexerResult(String fileName) throws IOException {
        return new Scanner2(deserializeLexerResult(fileName));
    }

    @Override
    public Token getNextToken() {
        if (index < tokens.size()) {
            return tokens.get(index++);
        }
        return Token.endToken;
    }

    @Override
    public Token peekNextToken() {
        if (index < tokens.size()) {
            return tokens.get(index);
        }
        return Token.endToken;
    }

    private static List<Token> deserializeLexerResult(String fileName) throws IOException {
        List<Token> result = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = in.readLine()) != null) {
                int separatorIndex = line.indexOf(" ");
                String rawTag = line.substring(0, separatorIndex);
                String rawValue = line.substring(separatorIndex + 1);
                int tag = Integer.parseInt(rawTag);
                TokenTag tokenTag = getTag(tag);
                String value = getValue(rawValue, tag);
                result.add(new Token(tokenTag, value));
            }
        }
        return result;
    }

    private static String getValue(String value, int tag) {
        if (tag == TokenTag.STRING_LITERAL_TAG.ordinal()) {
            System.out.println(value);
            return removeQuotes(value);
        }
        return value;
    }

    private static String removeQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    private static TokenTag getTag(int tag) {
        try{
            return Arrays.stream(TokenTag.values())
                    .filter(t -> t.ordinal() == tag)
                    .findFirst()
                    .orElseThrow();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Cannot find token with tag " + tag);
        }
    }
}
