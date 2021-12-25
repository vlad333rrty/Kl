package kalina.compiler.syntax.scanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public class TokenDeserializer implements IScanner {
    private final BufferedReader reader;
    private Token peekedToken;

    public TokenDeserializer(String filename) throws IOException {
        this.reader = new BufferedReader(new FileReader(filename));
    }

    @Override
    public Token getNextToken() {
        if (peekedToken != null) {
            Token t = peekedToken;
            peekedToken = null;
            return t;
        }
        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (line == null) {
            return Token.endToken;
        }
        String[] tagAndValue = line.split(" ");

        int tag = Integer.parseInt(tagAndValue[0]);
        String value = tagAndValue[1];

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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        reader.close();
    }
}
