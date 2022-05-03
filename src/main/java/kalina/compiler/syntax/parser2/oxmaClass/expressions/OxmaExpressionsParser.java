package kalina.compiler.syntax.parser2.oxmaClass.expressions;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.ast.expression.ASTArithmeticExpression;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTFactor;
import kalina.compiler.ast.expression.ASTFunCallExpression;
import kalina.compiler.ast.expression.ASTMethodCallExpression;
import kalina.compiler.ast.expression.ASTObjectCreationExpression;
import kalina.compiler.ast.expression.ASTTerm;
import kalina.compiler.ast.expression.ASTValueExpression;
import kalina.compiler.ast.expression.ASTVariableExpression;
import kalina.compiler.expressions.operations.ArithmeticOperation;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.Assert;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser.ParseUtils;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class OxmaExpressionsParser extends AbstractOxmaExpressionsParser {

    public OxmaExpressionsParser(IScanner scanner) {
        super(scanner);
    }

    @Override
    public ASTExpression parse() throws ParseException {
        if (peekNextToken().getTag() == TokenTag.NEW_TAG) {
            return parseNewInt();
        }
        if (peekNextToken().getTag() == TokenTag.STRING_LITERAL_TAG) {
            return new ASTValueExpression(getNextToken().getValue(), Type.getType(String.class));
        }
        if (peekNextToken().getTag() ==TokenTag.BOOL_VALUE_TAG) {
            return new ASTValueExpression(ParseUtils.getTrueValue(getNextToken()), Type.BOOLEAN_TYPE);
        }
        return parseAr();
    }

    private ASTArithmeticExpression parseAr() throws ParseException {
        List<ASTTerm> terms = new ArrayList<>();
        List<ArithmeticOperation> operations = new ArrayList<>();
        ASTTerm term = parseTerm();
        terms.add(term);
        while (peekNextToken().getTag() == TokenTag.PLUS_TAG || peekNextToken().getTag() == TokenTag.MINUS_TAG) {
            Token token = getNextToken();
            if (token.getTag() == TokenTag.PLUS_TAG) {
                operations.add(ArithmeticOperation.PLUS);
            } else {
                operations.add(ArithmeticOperation.MINUS);
            }
            terms.add(parseTerm());
        }

        return new ASTArithmeticExpression(terms, operations);
    }

    private ASTTerm parseTerm() throws ParseException {
        List<ASTFactor> factors = new ArrayList<>();
        List<ArithmeticOperation> operations = new ArrayList<>();
        ASTFactor factor = parseFactor();
        factors.add(factor);
        while (peekNextToken().getTag() == TokenTag.MUL_TAG || peekNextToken().getTag() == TokenTag.DIV_TAG) {
            Token token = getNextToken();
            if (token.getTag() == TokenTag.MUL_TAG) {
                operations.add(ArithmeticOperation.MULTIPLY);
            } else {
                operations.add(ArithmeticOperation.DIVIDE);
            }
            factors.add(parseFactor());
        }

        return new ASTTerm(factors, operations);
    }

    private ASTFactor parseFactor() throws ParseException {
        Token token = getNextToken();
        if (token.getTag() == TokenTag.NUMBER_TAG) {
            return ASTFactor.createFactor(new ASTValueExpression(ParseUtils.getTrueValue(token), Type.INT_TYPE));
        }
        if (token.getTag() == TokenTag.LONG_NUMBER_TAG) {
            return ASTFactor.createFactor(new ASTValueExpression(ParseUtils.getTrueValue(token), Type.LONG_TYPE));
        }
        if (token.getTag() == TokenTag.FLOAT_NUMBER_TAG) {
            return ASTFactor.createFactor(new ASTValueExpression(ParseUtils.getTrueValue(token), Type.FLOAT_TYPE));
        }
        if (token.getTag() == TokenTag.DOUBLE_NUMBER_TAG) {
            return ASTFactor.createFactor(new ASTValueExpression(ParseUtils.getTrueValue(token), Type.DOUBLE_TYPE));
        }
        if (token.getTag() == TokenTag.IDENT_TAG) {
            String name = token.getValue();
            if (peekNextToken().getTag() == TokenTag.LPAREN_TAG) {
                ASTExpression funCallExpression = parseFunCall(name);
                return ASTFactor.createFactor(funCallExpression);
            } else if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                getNextToken();
                Token methodName = getNextToken();
                Assert.assertTag(methodName, TokenTag.IDENT_TAG);

                ASTExpression methodCall = parseMethodCall(name, methodName.getValue());
                return ASTFactor.createFactor(methodCall);
            }

            return ASTFactor.createFactor(new ASTVariableExpression(name));
        }
        if (token.getTag() == TokenTag.LPAREN_TAG) {
            ASTExpression expression = parseAr();
            Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);
            return ASTFactor.createFactor(expression);
        }
        if (token.getTag() == TokenTag.MINUS_TAG) {
            ASTExpression expression = parseFactor();
            return ASTFactor.createNegateFactor(expression);
        }

        throw new ParseException("Unexpected token: " + token);
    }

    private ASTExpression parseNewInt() throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.NEW_TAG);
        Token token = getNextToken();

        String className = token.getValue();
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<ASTExpression> arguments = parseFunArgs();
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ASTObjectCreationExpression(className, arguments);
    }

    private List<ASTExpression> parseFunArgs() throws ParseException {
        if (peekNextToken().getTag() == TokenTag.RPAREN_TAG) {
            return List.of();
        }

        List<ASTExpression> expressions = new ArrayList<>();
        expressions.add(parse());
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            expressions.add(parse());
        }

        return expressions;
    }

    private ASTFunCallExpression parseFunCall(String funName) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<ASTExpression> args = parseFunArgs();
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ASTFunCallExpression(funName, args);
    }

    private ASTMethodCallExpression parseMethodCall(String ownerObjectName, String funName) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<ASTExpression> args = parseFunArgs();
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ASTMethodCallExpression(ownerObjectName, funName, args);
    }
}
