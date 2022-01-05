import java.io.FileOutputStream;
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

/**
 * @author vlad333rrty
 */
public class Main {
    public static void main(String[] args) throws IOException, ParseException, CodeGenException {
        String filename = "/home/vlad333rrty/IdeaProjects/KalinaLang/data/output.kl";
        AbstractParser parser = new RecursiveDescentParser(new Scanner(filename));
        ParseResult result = parser.parse();
        CodeGenerationManager codeGenerationManager = new CodeGenerationManager();
        if (result.getRoot().isPresent()) {
            ClassBasicBlock bb = result.getRoot().get();
            List<CodeGenerationResult> codeGenerationResults = codeGenerationManager.generateByteCode(bb);
            for (CodeGenerationResult res : codeGenerationResults) {
                writeToFile(res.getByteCode(), res.getClassName() + ".class");
            }
        }
    }

    private static void writeToFile(byte[] bytes, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
