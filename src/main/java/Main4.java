import java.io.IOException;
import java.util.List;

import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.traverse.ASTTraverser;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenerationManager;
import kalina.compiler.codegen.CodeGenerationResult;
import kalina.compiler.syntax.parser2.OxmaParser;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.scanner.Scanner;
import kalina.compiler.utils.FileUtils;

/**
 * @author vlad333rrty
 */
public class Main4 {
    public static void main(String[] args) {
        try {
            run("data/output.kl");
        } catch (IOException | ParseException | CFGConversionException | IncompatibleTypesException | CodeGenException e) {
            e.printStackTrace();
        }
    }

    public static void run(String outputFilePath)
            throws IOException, ParseException, CFGConversionException, CodeGenException, IncompatibleTypesException
    {
        OxmaParser parser = new OxmaParser(new Scanner(outputFilePath));
        ASTRootNode result = parser.parse();
        ASTTraverser traverser = new ASTTraverser();
        var root = traverser.traverse(result);

        CodeGenerationManager codeGenerationManager = new CodeGenerationManager();
        for (ClassBasicBlock bb : root.getClassBasicBlocks()) {
            List<CodeGenerationResult> codeGenerationResults = codeGenerationManager.generateByteCode(bb);
            for (CodeGenerationResult res : codeGenerationResults) {
                FileUtils.writeToFile(res.getClassName() + ".class", res.getByteCode());
            }
        }
    }
}
