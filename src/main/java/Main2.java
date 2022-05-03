import java.io.IOException;
import java.util.List;

import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.traverse.ASTTraverser;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenerationManager;
import kalina.compiler.codegen.CodeGenerationResult;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser2.OxmaParser;
import kalina.compiler.syntax.scanner.Scanner;
import kalina.compiler.utils.FileUtils;

/**
 * @author vlad333rrty
 */
public class Main2 {
    public static void main(String[] args) throws IOException, ParseException, CFGConversionException, CodeGenException {
        OxmaParser parser = new OxmaParser(new Scanner("data/output.kl"));
        var result = parser.parse();
        ASTTraverser astTraverser = new ASTTraverser();
        var rootBB = astTraverser.traverse(result);
        System.out.println(result);
        ClassBasicBlock bb = rootBB.getClassBasicBlocks().get(0);
        CodeGenerationManager codeGenerationManager = new CodeGenerationManager();
        List<CodeGenerationResult> codeGenerationResults = codeGenerationManager.generateByteCode(bb);
        for (CodeGenerationResult res : codeGenerationResults) {
            FileUtils.writeToFile(res.getClassName() + ".class", res.getByteCode());
        }
    }
}
