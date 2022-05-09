import java.io.IOException;
import java.util.List;

import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.bb.RootBasicBlock;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.traverse.ASTTraverser;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenerationManager;
import kalina.compiler.codegen.CodeGenerationResult;
import kalina.compiler.syntax.parser.ParseException;
import kalina.compiler.syntax.parser2.OxmaParser;
import kalina.compiler.syntax.scanner.Scanner;
import kalina.compiler.utils.FileUtils;
import kalina.internal.CFGTreePrinter;
import kalina.internal.DotGraphConstructor;

/**
 * @author vlad333rrty
 */
public class Main2 {
    public static void main(String[] args)
            throws IOException, ParseException, CFGConversionException, CodeGenException, IncompatibleTypesException
    {
        OxmaParser parser = new OxmaParser(new Scanner("data/output.kl"));
        ASTRootNode result = parser.parse();
        ASTTraverser astTraverser = new ASTTraverser();
        RootBasicBlock rootBB = astTraverser.traverse(result);
        DotGraphConstructor.plotGraph(result, "cfg.png");
        List<ClassBasicBlock> bbs = rootBB.getClassBasicBlocks();
        CodeGenerationManager codeGenerationManager = new CodeGenerationManager();
        for (ClassBasicBlock bb : bbs) {
            CFGTreePrinter.print(bb);
            List<CodeGenerationResult> codeGenerationResults = codeGenerationManager.generateByteCode(bb);
            for (CodeGenerationResult res : codeGenerationResults) {
                FileUtils.writeToFile(res.getClassName() + ".class", res.getByteCode());
            }
        }
    }
}
