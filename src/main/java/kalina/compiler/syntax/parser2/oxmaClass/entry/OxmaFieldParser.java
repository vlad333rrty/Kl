package kalina.compiler.syntax.parser2.oxmaClass.entry;

import kalina.compiler.ast.ASTNode;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public class OxmaFieldParser extends AbstractOxmaFieldParser {
    public OxmaFieldParser(IScanner scanner) {
        super(scanner);
    }

    @Override
    public ASTNode parse(boolean isStatic, String ownerClassName) throws ParseException {
        return null;
    }
}
