import java.io.IOException;
import java.util.List;

import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenerationManager;
import kalina.compiler.codegen.CodeGenerationResult;
import kalina.compiler.syntax.parser.AbstractParser;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser.ParseResult;
import kalina.compiler.syntax.parser.RecursiveDescentParser;
import kalina.compiler.syntax.scanner.Scanner;
import kalina.compiler.utils.FileUtils;
import kalina.internal.CFGTreePrinter;

/**
 * @author vlad333rrty
 */
public class Main {
    private static final String DEFAULT_PATH = "/home/vlad333rrty/IdeaProjects/KalinaLang/data/output.kl";

    public static void main(String[] args) throws IOException, ParseException, CodeGenException {
        final String path;
        if (args.length == 0) {
            path = DEFAULT_PATH;
        } else {
            path = args[0];
        }
        AbstractParser parser = new RecursiveDescentParser(new Scanner(path));
        ParseResult result = parser.parse();
        CodeGenerationManager codeGenerationManager = new CodeGenerationManager();
        if (result.getRoot().isPresent()) {
            CFGTreePrinter.print(result.getRoot().get());
            ClassBasicBlock bb = result.getRoot().get();
            List<CodeGenerationResult> codeGenerationResults = codeGenerationManager.generateByteCode(bb);
            for (CodeGenerationResult res : codeGenerationResults) {
                FileUtils.writeToFile(res.getClassName() + ".class", res.getByteCode());
            }
        }
    }
}
