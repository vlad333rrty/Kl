package kalina.compiler.syntax.parser2.oxmaClass;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.cfg.data.OxmaFunctionTable;
import kalina.compiler.cfg.data.OxmaFunctionTableImpl;
import kalina.compiler.syntax.build.TokenTag;
import kalina.compiler.syntax.parser2.Assert;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaFieldParser;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaMethodParser;
import kalina.compiler.syntax.parser2.oxmaClass.entry.OxmaMethodParserBase;
import kalina.compiler.syntax.parser2.oxmaClass.entry.rhs.OxmaRHSParser;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaConditionExpressionsParser;
import kalina.compiler.syntax.parser2.oxmaClass.expressions.OxmaExpressionsParser;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public class OxmaClassParser extends OxmaParserBase {
    private final OxmaMethodParserBase methodParser;
    @SuppressWarnings("all")
    private final OxmaFieldParser fieldParser;

    public OxmaClassParser(IScanner scanner) {
        super(scanner);
        OxmaExpressionsParser expressionsParser = new OxmaExpressionsParser(scanner);
        this.methodParser = new OxmaMethodParser(
                scanner,
                expressionsParser,
                new OxmaConditionExpressionsParser(scanner, expressionsParser),
                new OxmaRHSParser(scanner, expressionsParser));
        this.fieldParser = new OxmaFieldParser(scanner, expressionsParser);
    }

    public ASTClassNode parse() throws ParseException {
        Token token = getNextToken();
        Assert.assertTag(token, TokenTag.IDENT_TAG);
        String className = token.getValue();
        final String parentClassName;
        if (peekNextToken().getTag() == TokenTag.EXTENDS_TAG) {
            getNextToken(); // skip `extends`
            Token superClass = getNextToken();
            Assert.assertTag(superClass, TokenTag.IDENT_TAG);
            parentClassName = superClass.getValue();
        } else {
            parentClassName = "java/lang/Object";
        }

        Assert.assertTag(getNextToken(), TokenTag.LBRACE_TAG);

        OxmaFunctionTable functionTable = new OxmaFunctionTableImpl();
        ASTClassNode classNode = new ASTClassNode(parentClassName, className, functionTable);
        parseClassEntry(classNode, className, functionTable);

        Assert.assertTag(getNextToken(), TokenTag.RBRACE_TAG);

        return classNode;
    }

    private void parseClassEntry(ASTClassNode classNode, String className, OxmaFunctionTable functionTable) throws ParseException {
        Token token = peekNextToken();
        if (token.getTag() == TokenTag.RBRACE_TAG) {
            return;
        }
        getNextToken();
        switch (token.getTag()) {
            case BEGIN_TAG -> classNode.addChild(methodParser.parseBegin());
            case FUN_TAG -> classNode.addChild(methodParser.parse(false, className, functionTable, ClassEntryUtils.AccessModifier.PUBLIC, List.of()));
            case STATIC_TAG -> onStaticDetected(classNode, className, functionTable, ClassEntryUtils.AccessModifier.PUBLIC);
            case PRIVATE_TAG -> onAccessModifierDetected(classNode, className, functionTable, ClassEntryUtils.AccessModifier.PRIVATE);
            case PROTECTED_TAG -> onAccessModifierDetected(classNode, className, functionTable, ClassEntryUtils.AccessModifier.PROTECTED);
            case FINAL_TAG -> onFinalDetected(classNode, className, functionTable, ClassEntryUtils.AccessModifier.PUBLIC, List.of());
            default -> classNode.addChild(fieldParser.parseWithType(ClassEntryUtils.AccessModifier.PUBLIC, List.of(), token));
        }
        parseClassEntry(classNode, className, functionTable);
    }

    private void onAccessModifierDetected(ASTClassNode classNode, String className, OxmaFunctionTable functionTable, ClassEntryUtils.AccessModifier accessModifier) throws ParseException {
        Token token = peekNextToken();
        switch (token.getTag()) {
            case STATIC_TAG -> {
                getNextToken();
                onStaticDetected(classNode, className, functionTable, accessModifier);
            }
            case FINAL_TAG -> {
                getNextToken();
                onFinalDetected(classNode, className, functionTable, accessModifier, new ArrayList<>());
            }
            case FUN_TAG -> {
                getNextToken();
                classNode.addChild(methodParser.parse(false, className, functionTable, accessModifier, new ArrayList<>()));
            }
            case CONST_TAG -> {
                getNextToken();
                classNode.addChild(fieldParser.parse(accessModifier, List.of(ClassEntryUtils.Modifier.STATIC, ClassEntryUtils.Modifier.FINAL)));
            }
            default -> classNode.addChild(fieldParser.parse(accessModifier, List.of()));
        }
    }

    private void onFinalDetected(
            ASTClassNode classNode,
            String className,
            OxmaFunctionTable functionTable,
            ClassEntryUtils.AccessModifier accessModifier,
            List<ClassEntryUtils.Modifier> modifiers) throws ParseException
    {
        modifiers.add(ClassEntryUtils.Modifier.FINAL);
        Token token = peekNextToken();
        if (token.getTag() == TokenTag.FUN_TAG) {
            getNextToken();
            classNode.addChild(methodParser.parse(false, className, functionTable, accessModifier, modifiers));
        } else {
            classNode.addChild(fieldParser.parse(accessModifier, modifiers));
        }
    }

    private void onStaticDetected(ASTClassNode classNode, String className, OxmaFunctionTable functionTable, ClassEntryUtils.AccessModifier accessModifier) throws ParseException {
        List<ClassEntryUtils.Modifier> modifiers = new ArrayList<>(List.of(ClassEntryUtils.Modifier.STATIC));
        Token token = peekNextToken();
        switch (token.getTag()) {
            case FUN_TAG -> {
                getNextToken();
                classNode.addChild(methodParser.parse(true, className, functionTable, accessModifier, modifiers));
            }
            case FINAL_TAG -> {
                getNextToken();
                onFinalDetected(classNode, className, functionTable, accessModifier, modifiers);
            }
            default -> classNode.addChild(fieldParser.parse(accessModifier, modifiers));
        }
    }
}
