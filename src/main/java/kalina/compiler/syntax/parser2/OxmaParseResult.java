package kalina.compiler.syntax.parser2;

import kalina.compiler.ast.ASTNode;
import kalina.compiler.syntax.parser.ParsingStatus;

/**
 * @author vlad333rrty
 */
public record OxmaParseResult(ASTNode root, ParsingStatus status) {
}
