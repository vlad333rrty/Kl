package kalina.compiler.syntax.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kalina.compiler.syntax.tokens.Token;


public class GrammarHandler {
    private final IGrammar grammar;
    private final Map<NonTerminal, List<Set<Terminal>>> first = new HashMap<>();
    private final Map<NonTerminal, Set<Terminal>> follow = new HashMap<>();

    private final Map<NonTerminal, Set<Terminal>> firstSet = new HashMap<>();

    public GrammarHandler(IGrammar grammar) {
        this.grammar = grammar;
        buildFirst();
        buildFollow();
    }

    public List<Set<Terminal>> getFirst(NonTerminal x) {
        return first.get(x);
    }

    public Set<Terminal> getFollow(NonTerminal x) {
        return follow.get(x);
    }

    public Set<Terminal> getFirst(GObject gObject) {
        Set<Terminal> first = new HashSet<>();
        if (gObject instanceof Terminal) {
            first.add((Terminal) gObject);
        } else {
            List<Set<Terminal>> set = this.first.get(gObject);
            if (set != null) {
                for (Set<Terminal> s : set) {
                    first.addAll(s);
                }
            }
        }
        return first;
    }

    public IGrammar getGrammar() {
        return grammar;
    }

    private void buildFirst() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<NonTerminal, List<List<GObject>>> entry : grammar.getRules().entrySet()) {
                NonTerminal key = entry.getKey();
                List<Set<Terminal>> terminals = first.computeIfAbsent(key, k -> new ArrayList<>());
                Set<Terminal> terminalS = firstSet.computeIfAbsent(key, k -> new HashSet<>());
                int len = terminalS.size();
                for (List<GObject> rule : entry.getValue()) {
                    GObject gObject = rule.stream().findFirst().orElseThrow();
                    if (terminalS.addAll(getFirst(gObject))) {
                        terminals.add(new HashSet<>(getFirst(gObject)));
                    }
                }
                if (terminalS.size() != len) {
                    changed = true;
                }
            }
        }
    }

    private void buildFollow() {
        for (Map.Entry<NonTerminal, List<List<GObject>>> entry : grammar.getRules().entrySet()) {
            Set<Terminal> set = new HashSet<>();
            if (entry.getKey().getValue().equals(grammar.getAxiomName())) {
                set.add(new Terminal(Token.endToken.getValue()));
            }
            follow.put(entry.getKey(), set);
        }
        boolean changed;
        do {
            changed = false;
            for (Map.Entry<NonTerminal, List<List<GObject>>> entry : grammar.getRules().entrySet()) {
                for (List<GObject> rule : entry.getValue()) {
                    for (int i = 0; i < rule.size(); i++) {
                        GObject gObject = rule.get(i);
                        if (!(gObject instanceof NonTerminal)) {
                            continue;
                        }
                        NonTerminal nonTerminal = (NonTerminal) gObject;
                        Set<Terminal> follow = this.follow.get(nonTerminal);
                        int len = follow.size();
                        if (i < rule.size() - 1) {
                            Set<Terminal> first = getFirst(rule.get(i + 1));
                            if (first.stream().anyMatch(t -> t.getValue().equals(grammar.getEpsilonName()))) {
                                follow.addAll(this.follow.get(entry.getKey()));
                            }
                            follow.addAll(first);
                        } else {
                            follow.addAll(this.follow.get(entry.getKey()));
                        }
                        if (follow.size() != len) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
    }
}
