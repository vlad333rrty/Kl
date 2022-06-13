import java.io.IOException;
import java.util.Arrays;

import kalina.compiler.OxmaCompiler;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.syntax.parser2.ParseException;

/**
 * @author vlad333rrty
 */
public class Main2 {
    public static void main(String[] args)
            throws IOException, ParseException, CFGConversionException, CodeGenException, IncompatibleTypesException
    {
        if (args.length == 0) {
            throw new IllegalArgumentException("No input provided for the compiler! Lexer result expected");
        }
        new OxmaMain(OxmaCompiler.SettingsParser.parseCommandLineArgs(Arrays.copyOfRange(args, 1, args.length)))
                .run(args[0]);
    }
}
