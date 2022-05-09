package kalina.compiler.syntax.parser2.oxmaClass.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.ASTLHS;
import kalina.compiler.ast.ASTMethodEntryNode;
import kalina.compiler.ast.expression.ASTCondExpression;
import kalina.compiler.ast.expression.ASTDoInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.ast.expression.ASTInitInstruction;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser.Assert;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser.ParseUtils;
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
        Optional<ASTInitInstruction> varDecl;
        if (peekNextToken().getTag() == TokenTag.SEMICOLON_TAG) {
            varDecl = Optional.empty();
        } else {
            varDecl = Optional.of(parseVarDeclInt());
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

        return new ASTForInstruction(varDecl, cond, action, forEntry);
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

    private ASTInitInstruction parseVarDeclInt() throws ParseException {
        ASTLHS lhs = parseLHS();
        List<ASTExpression> rhs = parseRHS(lhs.size());
        return new ASTInitInstruction(lhs, rhs);
    }

    private ASTLHS parseLHS() throws ParseException {
        List<String> variableNames = new ArrayList<>();
        Token token = getNextToken();

        Assert.assertTrue(token, tag -> tag == TokenTag.IDENT_TAG || ParseUtils.isPrimitiveType(tag));

        Type convertedType = ParseUtils.convertRawType(token);
        variableNames.add(parseSingleVariableDeclaration());
        while (peekNextToken().getTag() == TokenTag.COMMA_TAG) {
            getNextToken();
            variableNames.add(parseSingleVariableDeclaration());
        }

        return new ASTLHS(variableNames, convertedType);
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