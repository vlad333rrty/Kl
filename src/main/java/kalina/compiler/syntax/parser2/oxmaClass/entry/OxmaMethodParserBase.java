package kalina.compiler.syntax.parser2.oxmaClass.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.ASTLHS;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.ast.ASTNode;
import kalina.compiler.ast.expression.ASTAssignInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTFunCallExpression;
import kalina.compiler.ast.expression.ASTInitInstruction;
import kalina.compiler.ast.expression.ASTMethodCallExpression;
import kalina.compiler.ast.expression.ASTObjectCreationExpression;
import kalina.compiler.ast.expression.ASTReturnInstruction;
import kalina.compiler.bb.TypeAndName;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.Assert;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser.ParseUtils;
import kalina.compiler.syntax.parser2.data.OxmaFunctionInfo;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaConditionExpressionsParser;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaExpressionsParser;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public abstract class OxmaMethodParserBase extends AbstractOxmaClassEntryParser {
    private static final Logger logger = LogManager.getLogger(OxmaMethodParserBase.class);

    protected final OxmaExpressionsParser expressionsParser;
    protected final OxmaConditionExpressionsParser conditionExpressionsParser;
    private final OxmaFunctionTable functionTable;

    public OxmaMethodParserBase(
            IScanner scanner,
            OxmaFunctionTable functionTable,
            OxmaExpressionsParser expressionsParser,
            OxmaConditionExpressionsParser conditionExpressionsParser)
    {
        super(scanner);
        this.functionTable = functionTable;
        this.expressionsParser = expressionsParser;
        this.conditionExpressionsParser = conditionExpressionsParser;
    }

    @Override
    public ASTMethodNode parse(boolean isStatic, String ownerClassName) throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String name = token.getValue();

        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<TypeAndName> args = parseFunArgs();
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        Type returnType;
        if (peekNextToken().getTag() == TokenTag.ARROW_TAG) {
            getNextToken();
            Token returnTypeToken = getNextToken();
            returnType = ParseUtils.convertRawType(returnTypeToken.getValue());
        } else {
            returnType = Type.VOID_TYPE;
        }
        OxmaFunctionInfo functionInfo = new OxmaFunctionInfo(args, returnType, ownerClassName,false, isStatic);
        functionTable.addFunction(name, functionInfo);

        ASTMethodNode methodNode = new ASTMethodNode(name, args, returnType, isStatic, 0);

        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        parseFunEntry(returnType, methodNode);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);

        return methodNode;
    }

    protected void parseFunEntry(
            Type returnType,
            ASTNode methodNode) throws ParseException
    {
        Token token = peekNextToken();
        ASTExpression expressionNode;
        switch (token.getTag()) {
            case IDENT_TAG -> {
                getNextToken();
                String identName = token.getValue();
                if (peekNextToken().getTag() == TokenTag.LPAREN_TAG) {
                    expressionNode = parseFunCall(token.getValue());
                } else if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                    getNextToken();
                    expressionNode = parseMethodCall(identName, getNextToken().getValue());
                } else if (peekNextToken().getTag() == TokenTag.ASSIGN_TAG) {
                    expressionNode = parseAssign(token.getValue());
                } else if (peekNextToken().getTag() == TokenTag.IDENT_TAG) {
                    expressionNode = parseVarDeclWithKnownType(identName);
                } else {
                    throw new IllegalArgumentException("Unexpected token: " + token.getValue());
                }
            }
            case SHORT_TAG, INT_TAG, LONG_TAG, FLOAT_TAG, DOUBLE_TAG, STRING_TAG, BOOL_TAG -> {
                getNextToken(); // skip `int`
                expressionNode = parseVarDeclWithKnownType(token.getValue());
            }
            case IF_TAG -> expressionNode = parseBranchStmt(returnType, TokenTag.IF_TAG);
            case FOR_TAG -> expressionNode = parseForStmt(returnType);
            case DO_TAG -> expressionNode = parseDoStmt(returnType);
            case NEW_TAG -> expressionNode = parseNew();
            case RETURN_TAG -> {
                getNextToken();
                expressionNode = onReturnDetected(returnType);
            }
            case RBRACE_TAG -> {
                return;
            }
            default -> throw new IllegalArgumentException("Unexpected token: " + token.getValue());
        }
        methodNode.addExpression(expressionNode);
        parseFunEntry(returnType, methodNode);
    }

    private ASTFunCallExpression parseFunCall(String funName) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<ASTExpression> args = parseFunCallArgs();
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ASTFunCallExpression(funName, args);
    }

    private List<TypeAndName> parseFunArgs() throws ParseException {
        if (peekNextToken().getTag() == TokenTag.RPAREN_TAG) {
            return List.of();
        }

        Token token = getNextToken();
        Type convertedType = ParseUtils.convertRawType(token.getValue());
        List<TypeAndName> typeAndNames = parseFunArgsInt(convertedType);
        List<TypeAndName> result = new ArrayList<>(typeAndNames);
        while (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            getNextToken();
            token = getNextToken();
            Type converted = ParseUtils.convertRawType(token.getValue());
            typeAndNames  = parseFunArgsInt(converted);
            result.addAll(typeAndNames);
        }

        return result;
    }

    private List<TypeAndName> parseFunArgsInt(Type type) throws ParseException {
        List<TypeAndName> typeAndNames = new ArrayList<>();
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        typeAndNames.add(new TypeAndName(type, token.getValue()));
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            token = getNextToken();
            Assert.assertTag(token, TokenTag.IDENT_TAG);
            typeAndNames.add(new TypeAndName(type, token.getValue()));
        }
        return typeAndNames;
    }

    private List<ASTExpression> parseFunCallArgs() throws ParseException {
        if (peekNextToken().getTag() == TokenTag.RPAREN_TAG) {
            return List.of();
        }

        List<ASTExpression> expressions = new ArrayList<>();
        expressions.add(expressionsParser.parse());
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            expressions.add(expressionsParser.parse());
        }

        return expressions;
    }

    private ASTMethodCallExpression parseMethodCall(String ownerObjectName, String funName) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<ASTExpression> args = parseFunCallArgs();
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ASTMethodCallExpression(ownerObjectName, funName, args);
    }

    protected ASTAssignInstruction parseAssign(String firstVarName) throws ParseException {
        List<String> lhs = parseVariableAssign(firstVarName);
        Assert.assertTag(getNextToken(), TokenTag.ASSIGN_TAG);
        List<ASTExpression> rhs = parseRHS(lhs.size());
        return new ASTAssignInstruction(lhs, rhs);
    }

    private List<String> parseVariableAssign(String firstVarName) throws ParseException {
        List<String> variableNames = new ArrayList<>();
        variableNames.add(firstVarName);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableNames.add(parseSingleVariableAssign());
        }

        return variableNames;
    }

    private String parseSingleVariableAssign() throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        return token.getValue();
    }

    protected List<ASTExpression> parseRHS(int lhsSize) throws ParseException {
        List<ASTExpression> expressions = new ArrayList<>();
        ASTExpression expression = expressionsParser.parse();

        expressions.add(expression);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            ASTExpression expr = expressionsParser.parse();
            expressions.add(expr);
        }

        if (expressions.size() != lhsSize) {
            throw new ParseException("Different number of variables and values in assign");
        }
        return expressions;
    }

    protected abstract ASTExpression parseBranchStmt(Type returnType, TokenTag brTag) throws ParseException;

    protected abstract ASTExpression parseForStmt(Type returnType) throws ParseException;

    protected abstract ASTExpression parseDoStmt(Type returnType) throws ParseException;

    private ASTReturnInstruction onReturnDetected(Type returnType) throws ParseException {
        if (returnType.getSort() == Type.VOID) {
            return new ASTReturnInstruction(Optional.empty());
        }
        ASTExpression returnExpression = expressionsParser.parse();
        return new ASTReturnInstruction(Optional.of(returnExpression));
    }

    private ASTInitInstruction parseVarDeclWithKnownType(String type) throws ParseException {
        Type convertedType = ParseUtils.convertRawType(type);
        ASTLHS lhs = parseLHSWithKnownType(convertedType);
        Token token = peekNextToken();
        List<ASTExpression> rhs;
        if (token.getTag() == TokenTag.ASSIGN_TAG) {
            getNextToken();
            rhs = parseRHS(lhs.size());
        } else {
            rhs = List.of();
        }

        return new ASTInitInstruction(lhs, rhs);
    }

    private ASTLHS parseLHSWithKnownType(Type convertedType) throws ParseException {
        List<String> variableInfos = new ArrayList<>();
        variableInfos.add(parseSingleVariableDeclaration());
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableInfos.add(parseSingleVariableDeclaration());
        }

        return new ASTLHS(variableInfos, convertedType);
    }

    protected String parseSingleVariableDeclaration() throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        return token.getValue();
    }

    private ASTExpression parseNew() throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.NEW_TAG);
        Token token = getNextToken();

        String className = token.getValue();
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);

        List<ASTExpression> arguments = parseFunCallArgs();
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ASTObjectCreationExpression(className, arguments);
    }
}
