package kalina.compiler.syntax.parser2.oxmaClass.entry;

import kalina.compiler.ast.ASTNode;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public abstract class AbstractOxmaFieldParser extends OxmaParserBase {
    protected AbstractOxmaFieldParser(IScanner scanner) {
        super(scanner);
    }

    public abstract ASTNode parse(boolean isStatic, String ownerClassName) throws ParseException;
}
