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

/**
 * @author vlad333rrty
 */
public class Main {
    public static void main(String[] args) throws IOException, ParseException, CodeGenException {
        if (args.length == 0) {
            throw new IllegalArgumentException("No .kl file provided");
        }
        AbstractParser parser = new RecursiveDescentParser(new Scanner(args[0]));
        ParseResult result = parser.parse();
        CodeGenerationManager codeGenerationManager = new CodeGenerationManager();
        if (result.getRoot().isPresent()) {
            ClassBasicBlock bb = result.getRoot().get();
            List<CodeGenerationResult> codeGenerationResults = codeGenerationManager.generateByteCode(bb);
            for (CodeGenerationResult res : codeGenerationResults) {
                FileUtils.writeToFile(res.getClassName() + ".class", res.getByteCode());
            }
        }
    }
}
