import java.io.IOException;

import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.syntax.parser.ParseException;

/**
 * @author vlad333rrty
 */
public class Main2 {
    public static void main(String[] args)
            throws IOException, ParseException, CFGConversionException, CodeGenException, IncompatibleTypesException
    {
        new OxmaMain().run("data/output.kl");
    }
}
