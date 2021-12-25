package kalina.compiler.syntax.parser;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import kalina.compiler.syntax.grammar.Axiom;
import kalina.compiler.syntax.grammar.GObject;
import kalina.compiler.syntax.grammar.IGrammar;
import kalina.compiler.syntax.grammar.NonTerminal;
import kalina.compiler.syntax.grammar.Terminal;
import kalina.compiler.syntax.scanner.IScanner;
import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public abstract class AbstractTopDownParser extends AbstractParser {

    protected AbstractTopDownParser(IScanner scanner) {
        super(scanner);
    }

    @Override
    public ParseResult parse(IGrammar grammar) {
        Stack<GObject> stack = new Stack<>();

        Terminal end = new Terminal(Token.endToken.getValue());
        Axiom start = new Axiom(grammar.getAxiomName());

        stack.push(end);
        stack.push(start);

        Token token = getNextToken();
        Map<NonTerminal, Map<String, List<GObject>>> table = buildTable(grammar);
        GObject top;
        do {
            top = stack.pop();
            if (top instanceof NonTerminal) {
                String val = getTokenTag(token);
                List<GObject> rule = table.get(top).get(val);
                if (rule == null || rule.isEmpty()) {
                    return new ParseResult(null, ParsingStatus.FAILURE);
                }

                for (int i = rule.size() - 1; i >= 0; i--) {
                    stack.push(rule.get(i));
                }
            } else {
                if (top.getValue().equals(grammar.getEpsilonName())) {
                    continue;
                }
                if (!areEqual((Terminal) top, token)) {
                    return new ParseResult(null, ParsingStatus.FAILURE);
                }

                token = getNextToken();
            }
        } while (!top.getValue().equals(Token.endToken.getValue()));

        return new ParseResult(null, ParsingStatus.SUCCESS);
    }

    protected abstract Map<NonTerminal, Map<String, List<GObject>>> buildTable(IGrammar grammar);
}
