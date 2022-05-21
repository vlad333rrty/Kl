import java.io.IOException;
import java.util.List;

import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.bb.RootBasicBlock;
import kalina.compiler.cfg.builder.CFGBuilder;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.traverse.ASTTraverser;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenerationManager;
import kalina.compiler.codegen.CodeGenerationResult;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.parser2.OxmaParser;
import kalina.compiler.syntax.scanner.Scanner;
import kalina.compiler.utils.FileUtils;
import kalina.internal.CFGDotGraphConstructor;
import kalina.internal.CFGTreePrinter;
import kalina.internal.ASTDotGraphConstructor;

/**
 * @author vlad333rrty
 */
public class OxmaMain {
    public void run(String outputFilePath)
            throws IOException, ParseException, CFGConversionException, CodeGenException, IncompatibleTypesException
    {
        OxmaParser parser = new OxmaParser(new Scanner(outputFilePath));
        ASTRootNode result = parser.parse();

        CFGBuilder cfgBuilder = new CFGBuilder();
        var t = cfgBuilder.build(result);
        CFGDotGraphConstructor.plotGraph(t.get(0), "cfg.png");

        ASTTraverser astTraverser = new ASTTraverser();
        RootBasicBlock rootBB = astTraverser.traverse(result);
        ASTDotGraphConstructor.plotGraph(result, "ast.png");
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
