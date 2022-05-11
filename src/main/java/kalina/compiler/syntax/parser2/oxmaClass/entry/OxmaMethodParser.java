package kalina.compiler.syntax.parser2.oxmaClass.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.ASTLHS;
import kalina.compiler.ast.ASTMethodEntryNode;
import kalina.compiler.ast.expression.ASTAssignInstruction;
import kalina.compiler.ast.expression.ASTCondExpression;
import kalina.compiler.ast.expression.ASTDoInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.ast.expression.ASTInitInstruction;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser2.Assert;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.ParseUtils;
import kalina.compiler.syntax.parser2.oxmaClass.entry.rhs.OxmaRHSParser;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaConditionExpressionsParser;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaExpressionsParser;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class OxmaMethodParser extends OxmaMethodParserBase {
    public OxmaMethodParser(
            IScanner scanner,
            OxmaExpressionsParser expressionsParser,
            OxmaConditionExpressionsParser conditionExpressionsParser,
            OxmaRHSParser rhsParser)
    {
        super(scanner, expressionsParser, conditionExpressionsParser, rhsParser);
    }

    @Override
    protected ASTExpression parseBranchStmt(Type returnType, TokenTag brTag) throws ParseException {
        Assert.assertTag(getNextToken(), brTag);
        ASTCondExpression cond = conditionExpressionsParser.parse();
        ASTMethodEntryNode thenBr = parseBranchEntry(returnType);
        ASTMethodEntryNode elseBr = null;
        if (peekNextToken().getTag() == TokenTag.ELSE_TAG) {
            getNextToken();
            elseBr = parseBranchEntry(returnType);
        } else if (peekNextToken().getTag() == TokenTag.ELIF_TAG) {
            elseBr = new ASTMethodEntryNode();
            ASTExpression elif = parseBranchStmt(returnType, TokenTag.ELIF_TAG);
            elseBr.addExpression(elif);
        }

        return new ASTIfInstruction(cond, thenBr, Optional.ofNullable(elseBr));
    }

    @Override
    protected ASTExpression parseForStmt(Type returnType) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.FOR_TAG);
        Optional<ASTExpression> declarationsOrAssigns;
        if (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            declarationsOrAssigns = Optional.empty();
        } else {
            declarationsOrAssigns = Optional.of(parseForStmtDeclsOrAssigns());
        }
        Assert.assertTag(getNextToken(), TokenTag.SEMICOLON_TAG);
        Optional<ASTCondExpression> cond;
        if (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            cond = Optional.empty();
        } else {
            cond = Optional.of(conditionExpressionsParser.parse());
        }
        Assert.assertTag(getNextToken(), TokenTag.SEMICOLON_TAG);
        Optional<ASTExpression> action;
        if (peekNextToken().getTag() == TokenTag.LBRACE_TAG) {
            action = Optional.empty();
        } else {
            action = Optional.of(parseAction());
        }

        ASTMethodEntryNode forEntry = new ASTMethodEntryNode();
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        parseFunEntry(returnType, forEntry);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);

        return new ASTForInstruction(declarationsOrAssigns, cond, action, forEntry);
    }

    @Override
    protected ASTExpression parseDoStmt(Type returnType) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.DO_TAG);
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);

        ASTMethodEntryNode doEntry = new ASTMethodEntryNode();
        parseFunEntry(returnType, doEntry);

        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);
        Assert.assertTag(getNextToken(), TokenTag.WHILE_TAG);
        ASTCondExpression cond = conditionExpressionsParser.parse();
        return new ASTDoInstruction(doEntry, cond);
    }

    private ASTMethodEntryNode parseBranchEntry(Type returnType) throws ParseException {
        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);
        ASTMethodEntryNode branch = new ASTMethodEntryNode();
        parseFunEntry(returnType, branch);
        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);

        return branch;
    }

    private ASTExpression parseForStmtDeclsOrAssigns() throws ParseException {
        Token token = getNextToken();
        if ((token.getTag() == TokenTag.IDENT_TAG || ParseUtils.isPrimitiveType(token.getTag()))
                && peekNextToken().getTag() == TokenTag.IDENT_TAG)
        {
            return parseForStmtInits(token);
        }
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        return parseForStmtAssigns(token.getValue());
    }

    private List<String> parseForStmtLHS() throws ParseException {
        List<String> variableNames = new ArrayList<>();
        variableNames.add(parseSingleVariableDeclaration());
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableNames.add(parseSingleVariableDeclaration());
        }
        return variableNames;
    }

    private List<String> parseForStmtLHSWithFirstVariable(String firstVarName) throws ParseException {
        List<String> variableNames = new ArrayList<>();
        variableNames.add(firstVarName);
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableNames.add(parseSingleVariableDeclaration());
        }
        return variableNames;
    }

    private ASTExpression parseForStmtInits(Token typeToken) throws ParseException {
        Type convertedType = ParseUtils.convertRawType(typeToken);
        List<String> variableNames = parseForStmtLHS();
        ASTLHS lhs = new ASTLHS(variableNames, convertedType);
        List<ASTExpression> rhs = parseRHS(lhs.size());
        return new ASTInitInstruction(lhs, rhs);
    }

    private ASTExpression parseForStmtAssigns(String firstVarName) throws ParseException {
        List<String> variableNames = parseForStmtLHSWithFirstVariable(firstVarName);
        List<ASTExpression> rhs = parseRHS(variableNames.size());
        return new ASTAssignInstruction(variableNames, rhs);
    }

    private ASTExpression parseAction() throws ParseException {
        String value = getNextToken().getValue();
        if (peekNextToken().getTag() == TokenTag.LPAREN_TAG) {
            throw new IllegalArgumentException();
        } else {
            return parseAssign(value);
        }
    }
}
