package kalina.compiler.syntax.parser;

import kalina.compiler.syntax.grammar.IGrammar;

/**
 * @author vlad333rrty
 */
public interface IParser {
    ParseResult parse(IGrammar grammar) throws ParseException;
}