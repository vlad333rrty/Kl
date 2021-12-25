package kalina.compiler.syntax.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kalina.compiler.syntax.tokens.Token;

/**
 * @author vlad333rrty
 */
public class Grammar implements IGrammar {
    private static final String DEFAULT_AXIOM_NAME = "START";
    private static final String DEFAULT_EPSILON_NAME = "e";

    private final Map<NonTerminal, List<List<GObject>>> rules;
    private final Set<Terminal> terminals;
    private final Set<NonTerminal> nonTerminals;
    private final String axiomName;
    private final String epsilonName;

    private Grammar(
            Map<NonTerminal, List<List<GObject>>> rules,
            Set<Terminal> terminals,
            Set<NonTerminal> nonTerminals,
            String axiomName,
            String epsilonName)
    {
        this.rules = rules;
        this.terminals = terminals;
        this.nonTerminals = nonTerminals;
        this.axiomName = axiomName;
        this.epsilonName = epsilonName;
    }

    public static Grammar parseFromInputString(String inputGrammar) {
        inputGrammar = inputGrammar.replaceAll(" ", "");

        Pattern pattern = Pattern.compile("(?<left>[A-Z_][A-Z0-9_]*)=(?<right>.+)");
        Matcher matcher = pattern.matcher(inputGrammar);
        Pattern right = Pattern.compile("(.+?)\\|");

        NonTerminal nonTerminal;
        Map<String, GObject> stringToGObject = new HashMap<>();

        final Set<Terminal> terminals = new HashSet<>();
        final Set<NonTerminal> nonTerminals = new HashSet<>();
        final Map<NonTerminal, List<List<GObject>>> rules = new HashMap<>();

        while (matcher.find()) {
            String nonTerminalName = matcher.group("left");
            if ((nonTerminal = (NonTerminal) stringToGObject.get(nonTerminalName)) == null) {
                nonTerminal = new NonTerminal(nonTerminalName);
                stringToGObject.put(nonTerminalName, nonTerminal);
                nonTerminals.add(nonTerminal);
            }
            rules.put(nonTerminal, new ArrayList<>());
            Matcher rightSideMatcher = right.matcher(matcher.group("right"));
            List<List<GObject>> rightSide = rules.get(nonTerminal);
            while (rightSideMatcher.find()) {
                rightSide.add(new ArrayList<>());
                String rule = rightSideMatcher.group(1);
                Pattern gObjects = Pattern.compile("(<(?<nonTerminal>[A-Z0-9_]+)>)|(\\((?<terminal>[A-Z0-9_a-z]+)\\))");
                Matcher gObjectMatcher = gObjects.matcher(rule);
                while (gObjectMatcher.find()) {
                    String nextTerminalName = gObjectMatcher.group("terminal");
                    String nextNonTerminalName = gObjectMatcher.group("nonTerminal");
                    if (nextNonTerminalName != null) {
                        NonTerminal nextNonTerminal = (NonTerminal) stringToGObject.get(nextTerminalName);
                        if (nextNonTerminal == null) {
                            nextNonTerminal = new NonTerminal(nextNonTerminalName);
                            stringToGObject.put(nextNonTerminalName, nextNonTerminal);
                            nonTerminals.add(nextNonTerminal);
                        }
                        rightSide.get(rightSide.size() - 1).add(nextNonTerminal);
                    } else if (nextTerminalName != null) {
                        Terminal nextTerminal = (Terminal) stringToGObject.get(nextTerminalName);
                        if (nextTerminal == null) {
                            nextTerminal = new Terminal(nextTerminalName);
                            stringToGObject.put(nextTerminalName, nextTerminal);
                            terminals.add(nextTerminal);
                        }
                        rightSide.get(rightSide.size() - 1).add(nextTerminal);
                    }
                }
            }
        }
        terminals.add(new Terminal(Token.endToken.getValue()));

        String axiomName = getAxiomName(inputGrammar);
        String epsilonName = getEpsilonName(inputGrammar);
        return new Grammar(rules, terminals, nonTerminals, axiomName, epsilonName);
    }

    private static String getAxiomName(String inputGrammar) {
        Pattern pattern = Pattern.compile("start=(?<axiom>[A-Z][A-Z0-9]*)");
        Matcher matcher = pattern.matcher(inputGrammar);
        return matcher.find() ? matcher.group("axiom") : DEFAULT_AXIOM_NAME;
    }

    private static String getEpsilonName(String inputGrammar) {
        Pattern pattern = Pattern.compile("epsilon=(?<epsilon>\\p{L}\\w*)");
        Matcher matcher = pattern.matcher(inputGrammar);
        return matcher.find() ? matcher.group("epsilon") : DEFAULT_EPSILON_NAME;
    }

    @Override
    public Map<NonTerminal, List<List<GObject>>> getRules() {
        return rules;
    }

    @Override
    public Set<Terminal> getTerminals() {
        return terminals;
    }

    @Override
    public Set<NonTerminal> getNonTerminals() {
        return nonTerminals;
    }

    @Override
    public String getAxiomName() {
        return axiomName;
    }

    @Override
    public String getEpsilonName() {
        return epsilonName;
    }
}
