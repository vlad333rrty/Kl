package kalina.compiler.syntax.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kalina.compiler.syntax.grammar.GObject;
import kalina.compiler.syntax.grammar.GrammarHandler;
import kalina.compiler.syntax.grammar.IGrammar;
import kalina.compiler.syntax.grammar.NonTerminal;
import kalina.compiler.syntax.grammar.Terminal;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public class TopDownParser extends AbstractTopDownParser {

    public TopDownParser(IScanner scanner) {
        super(scanner);
    }

    @Override
    protected Map<NonTerminal, Map<String, List<GObject>>> buildTable(IGrammar grammar) {
        GrammarHandler grammarHandler = new GrammarHandler(grammar);
        Map<NonTerminal, Map<String, List<GObject>>> table = createTable(grammarHandler);
        for (Map.Entry<NonTerminal, List<List<GObject>>> entry : grammar.getRules().entrySet()) {
            NonTerminal nonTerminal = entry.getKey();
            final Map<String, List<GObject>> tableSection = table.get(nonTerminal);
            for (List<GObject> rulePart : entry.getValue()) {
                Set<Terminal> first = grammarHandler.getFirst(rulePart.stream().findFirst().orElseThrow());
                first.forEach(terminal -> tableSection.get(terminal.getValue()).addAll(rulePart));
                if (first.stream().anyMatch(terminal -> isEpsilon(terminal, grammar))) {
                    Set<Terminal> follow = grammarHandler.getFollow(nonTerminal);
                    follow.forEach(terminal -> tableSection.get(terminal.getValue()).addAll(rulePart));
                }
            }
        }

        return table;
    }

    private boolean isEpsilon(GObject gObject, IGrammar grammar) {
        return gObject.getValue().equals(grammar.getEpsilonName());
    }

    private Map<NonTerminal, Map<String, List<GObject>>> createTable(GrammarHandler grammarHandler) {
        Map<NonTerminal, Map<String, List<GObject>>> table = new HashMap<>();
        for (NonTerminal nonTerminal : grammarHandler.getGrammar().getNonTerminals()) {
            Map<String, List<GObject>> map = new HashMap<>();
            for (Terminal terminal : grammarHandler.getGrammar().getTerminals()) {
                map.put(terminal.getValue(), new ArrayList<>());
            }
            table.put(nonTerminal, map);
        }

        return table;
    }
}
