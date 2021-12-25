package kalina.compiler.syntax.grammar;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author vlad333rrty
 */
public interface IGrammar {
    Map<NonTerminal, List<List<GObject>>> getRules();
    Set<Terminal> getTerminals();
    Set<NonTerminal> getNonTerminals();
    String getAxiomName();
    String getEpsilonName();
}

