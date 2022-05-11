package kalina.compiler.syntax.parser2.oxmaClass.entry;

import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.ast.ASTNode;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.OxmaParserBase;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;
import kalina.compiler.syntax.scanner.IScanner;

/**
 * @author vlad333rrty
 */
public abstract class AbstractOxmaMethodParser extends OxmaParserBase {

    protected AbstractOxmaMethodParser(IScanner scanner) {
        super(scanner);
    }

    public abstract ASTMethodNode parseBegin(String ownerClassName) throws ParseException;

    public abstract ASTNode parse(boolean isStatic, String ownerClassName, OxmaFunctionTable functionTable) throws ParseException;
}
