package kalina.compiler.syntax.parser2.oxmaClass.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.ASTLHS;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.ast.ASTNode;
import kalina.compiler.ast.expression.ASTAssignInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTInitInstruction;
import kalina.compiler.ast.expression.ASTObjectCreationExpression;
import kalina.compiler.ast.expression.ASTReturnInstruction;
import kalina.compiler.ast.expression.ASTStandAloneFunCallExpression;
import kalina.compiler.ast.expression.ASTVariableExpression;
import kalina.compiler.ast.expression.array.ASTArrayAssignInstruction;
import kalina.compiler.ast.expression.array.ASTArrayLHS;
import kalina.compiler.bb.TypeAndName;
import kalina.compiler.cfg.data.OxmaFunctionInfo;
import kalina.compiler.cfg.data.OxmaFunctionTable;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser2.Assert;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.ParseUtils;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import kalina.compiler.syntax.parser2.oxmaClass.entry.rhs.OxmaRHSParser;
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
public abstract class OxmaMethodParserBase extends OxmaParserBase {
    private static final Logger logger = LogManager.getLogger(OxmaMethodParserBase.class);

    private static final String BEGIN_BLOCK_INNER_NAME = "main";

    protected final OxmaExpressionsParser expressionsParser;
    protected final OxmaConditionExpressionsParser conditionExpressionsParser;
    protected final OxmaRHSParser rhsParser;

    public OxmaMethodParserBase(
            IScanner scanner,
            OxmaExpressionsParser expressionsParser,
            OxmaConditionExpressionsParser conditionExpressionsParser,
            OxmaRHSParser rhsParser)
    {
        super(scanner);
        this.expressionsParser = expressionsParser;
        this.conditionExpressionsParser = conditionExpressionsParser;
        this.rhsParser = rhsParser;
    }

    public ASTMethodNode parse(
            boolean isStatic,
            String ownerClassName,
            OxmaFunctionTable functionTable,
            ClassEntryUtils.AccessModifier accessModifier,
            List<ClassEntryUtils.Modifier> modifiers) throws ParseException
    {
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
            returnType = ParseUtils.convertRawType(returnTypeToken);
        } else {
            returnType = Type.VOID_TYPE;
        }
        OxmaFunctionInfo functionInfo = new OxmaFunctionInfo(args, returnType, ownerClassName,false, isStatic);
        functionTable.addFunction(name, functionInfo);

        ASTMethodNode methodNode = new ASTMethodNode(name, args, returnType, isStatic, accessModifier, modifiers);

        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        parseFunEntry(returnType, methodNode);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        methodNode.addExpression(new ASTReturnInstruction(Optional.empty()));

        return methodNode;
    }

    public ASTMethodNode parseBegin() throws ParseException {
        Type returnType = Type.VOID_TYPE;
        ASTMethodNode methodNode = new ASTMethodNode(
                BEGIN_BLOCK_INNER_NAME,
                List.of(new TypeAndName(Type.getType(String[].class), "args")),
                returnType,
                true,
                ClassEntryUtils.AccessModifier.PUBLIC,
                List.of(ClassEntryUtils.Modifier.STATIC));

        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        parseFunEntry(returnType, methodNode);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        methodNode.addExpression(new ASTReturnInstruction(Optional.empty()));

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
                    if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                        getNextToken();
                        expressionNode = expressionsParser.parsePropertyCall(expressionNode);
                    }
                } else if (peekNextToken().getTag() == TokenTag.LEFT_SQ_BR_TAG) {
                    expressionNode = parseArrayAssignOrArrayElementAccess(identName);
                    if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                        getNextToken();
                        expressionNode = expressionsParser.parsePropertyCall(expressionNode);
                    }
                } else if (peekNextToken().getTag() == TokenTag.ASSIGN_TAG) {
                    expressionNode = parseAssign(token.getValue());
                } else if (peekNextToken().getTag() == TokenTag.IDENT_TAG) {
                    expressionNode = parseVarDeclWithKnownType(token);
                } else if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                    getNextToken();
                    expressionNode = expressionsParser.parsePropertyCall(new ASTVariableExpression(identName));
                } else {
                    throw new IllegalArgumentException("Unexpected token: " + token.getValue());
                }
            }
            case THIS_TAG -> {
                getNextToken();
                if (peekNextToken().getTag() == TokenTag.DOT_TAG) {
                    getNextToken();
                    expressionNode = expressionsParser.parsePropertyCallOfThis();
                } else {
                    throw new IllegalArgumentException("Unexpected token: " + peekNextToken());
                }
            }
            case SHORT_TAG, INT_TAG, LONG_TAG, FLOAT_TAG, DOUBLE_TAG, STRING_TAG, BOOL_TAG, ARRAY_TYPE_TAG -> {
                getNextToken(); // skip `int`
                expressionNode = parseVarDeclWithKnownType(token);
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

    private ASTStandAloneFunCallExpression parseFunCall(String funName) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.LPAREN_TAG);
        List<ASTExpression> args = parseFunCallArgs();
        Assert.assertTag(getNextToken(), TokenTag.RPAREN_TAG);

        return new ASTStandAloneFunCallExpression(funName, args);
    }

    private List<TypeAndName> parseFunArgs() throws ParseException {
        if (peekNextToken().getTag() == TokenTag.RPAREN_TAG) {
            return List.of();
        }

        Token token = getNextToken();
        Type convertedType = ParseUtils.convertRawType(token);
        List<TypeAndName> typeAndNames = parseFunArgsInt(convertedType);
        List<TypeAndName> result = new ArrayList<>(typeAndNames);
        while (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            getNextToken();
            token = getNextToken();
            Type converted = ParseUtils.convertRawType(token);
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

    protected ASTAssignInstruction parseAssign(String firstVarName) throws ParseException {
        List<String> lhs = parseVariableAssign(firstVarName);
        Assert.assertTag(getNextToken(), TokenTag.ASSIGN_TAG);
        List<ASTExpression> rhs = rhsParser.parseRHS(lhs.size());
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

    private ASTInitInstruction parseVarDeclWithKnownType(Token type) throws ParseException {
        Type convertedType = ParseUtils.convertRawType(type);
        ASTLHS lhs = parseLHSWithKnownType(convertedType);
        List<ASTExpression> rhs = parseRHS(lhs.size());
        return new ASTInitInstruction(lhs, rhs);
    }

    protected List<ASTExpression> parseRHS(int lhsSize) throws ParseException {
        Token token = peekNextToken();
        List<ASTExpression> rhs;
        if (token.getTag() == TokenTag.ASSIGN_TAG) {
            getNextToken();
            rhs = rhsParser.parseRHS(lhsSize);
        } else {
            rhs = List.of();
        }
        return rhs;
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

    public ASTExpression parseArrayAssignOrArrayElementAccess(String arrayName) throws ParseException {
        List<ASTExpression> indices = expressionsParser.parseArrayGetElement();
        ASTArrayLHS lhs = new ASTArrayLHS(arrayName, indices);
        if (peekNextToken().getTag() == TokenTag.DO_TAG) {
            throw new UnsupportedOperationException("Cannot call class method from array variable");
        } else if (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            return parseArrayLHS(lhs);
        } else if (peekNextToken().getTag() == TokenTag.ASSIGN_TAG) {
            getNextToken(); // slip `=`
            List<ASTExpression> rhs = rhsParser.parseRHS(1);
            return new ASTArrayAssignInstruction(List.of(lhs), rhs);
        }
        throw new IllegalArgumentException("Unexpected token: " + peekNextToken());
    }

    private ASTExpression parseArrayLHS(ASTArrayLHS initLHS) throws ParseException {
        List<ASTArrayLHS> lhs = new ArrayList<>();
        lhs.add(initLHS);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken(); // skip `,`
            Token token = getNextToken();
            Assert.assertTag(token, TokenTag.IDENT_TAG);
            List<ASTExpression> indices = expressionsParser.parseArrayGetElement();
            lhs.add(new ASTArrayLHS(token.getValue(), indices));
        }
        Assert.assertTag(getNextToken(), TokenTag.ASSIGN_TAG);
        List<ASTExpression> rhs = rhsParser.parseRHS(lhs.size());
        return new ASTArrayAssignInstruction(lhs, rhs);
    }
}
