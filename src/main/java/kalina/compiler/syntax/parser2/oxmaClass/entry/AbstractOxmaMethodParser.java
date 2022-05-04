package kalina.compiler.syntax.parser2.oxmaClass.entry;

import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public abstract class AbstractOxmaMethodParser extends AbstractOxmaClassEntryParser {

    protected AbstractOxmaMethodParser(IScanner scanner) {
        super(scanner);
    }

    public abstract ASTMethodNode parseBegin(String ownerClassName) throws ParseException;
}
