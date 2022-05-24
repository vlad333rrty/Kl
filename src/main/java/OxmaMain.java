import java.io.IOException;
import java.util.List;

import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.v2.ClassBasicBlock;
import kalina.compiler.cfg.builder.CFGBuilder;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.optimizations.OptimizationManager;
import kalina.compiler.cfg.optimizations.OptimizationManagerFactory;
import kalina.compiler.cfg.ssa.SSAFormBuilder;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenerationResult;
import kalina.compiler.codegen.v2.CFGByteCodeTranslator;
import kalina.compiler.codegen.v2.CodeGenerationManager;
import kalina.compiler.syntax.parser2.OxmaParser;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.scanner.Scanner;
import kalina.compiler.utils.FileUtils;
import kalina.internal.CFGDotGraphConstructor;

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
        List<ClassBasicBlock> bbs = cfgBuilder.build(result);

        AbstractCFGNode root = bbs.get(0).getEntry().get(0).getCfgRoot();
        SSAFormBuilder formBuilder = new SSAFormBuilder();
        formBuilder.buildSSA(root);

        OptimizationManager optimizationManager = OptimizationManagerFactory.create(root);
        optimizationManager.optimize();

        CFGDotGraphConstructor.plotMany(bbs);

        CodeGenerationManager codeGenerationManager = new CodeGenerationManager(new CFGByteCodeTranslator());
        for (ClassBasicBlock bb : bbs) {
            List<CodeGenerationResult> codeGenerationResults = codeGenerationManager.generateByteCode(bb);
            for (CodeGenerationResult res : codeGenerationResults) {
                FileUtils.writeToFile(res.getClassName() + ".class", res.getByteCode());
            }
        }
    }
}
