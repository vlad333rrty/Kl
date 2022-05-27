package kalina.compiler;

import java.io.IOException;
import java.util.List;

import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.v2.ClassBasicBlock;
import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.builder.CFGBuilder;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenerationResult;
import kalina.compiler.codegen.v2.CodeGenerationManager;
import kalina.compiler.syntax.parser2.OxmaParser;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.scanner.Scanner;
import kalina.compiler.utils.FileUtils;
import kalina.internal.CFGDotGraphConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public abstract class OxmaCompiler {
    private static final Logger logger = LogManager.getLogger(OxmaCompiler.class);

    private final OxmaCompilerSettings settings;

    public OxmaCompiler() {
        this.settings = OxmaCompilerSettings.defaultSettings();
    }

    public OxmaCompiler(OxmaCompilerSettings settings) {
        this.settings = settings;
    }

    public final void run(String outputFilePath)
            throws IOException, ParseException, CFGConversionException, CodeGenException, IncompatibleTypesException
    {
        OxmaParser parser = new OxmaParser(new Scanner(outputFilePath));
        ASTRootNode result = parser.parse();
        CFGBuilder cfgBuilder = new CFGBuilder();
        List<ClassBasicBlock> classBasicBlocks = cfgBuilder.build(result);

        if (settings.shouldPerformOptimizations || settings.shouldBuildSSAForm) {
            performCodeTransformations(classBasicBlocks);
        }
        if (settings.shouldPlotCFGs) {
            CFGDotGraphConstructor.plotMany(classBasicBlocks, settings.cfgPictureRelativePathBase);
        }

        CodeGenerationManager codeGenerationManager = new CodeGenerationManager();
        for (ClassBasicBlock bb : classBasicBlocks) {
            List<CodeGenerationResult> codeGenerationResults = codeGenerationManager.generateByteCode(bb);
            for (CodeGenerationResult res : codeGenerationResults) {
                FileUtils.writeToFile(
                        settings.compiledClassesDestinationDirectory + res.getClassName() + ".class",
                        res.getByteCode()
                );
            }
        }
    }

    private void performCodeTransformations(List<ClassBasicBlock> classBasicBlocks) {
        for (var classBb : classBasicBlocks) {
            for (var funBb : classBb.getEntry()) {
                ControlFlowGraph controlFlowGraph = ControlFlowGraph.fromRoot(funBb.getCfgRoot());
                logger.info("Starting building SSA form");
                buildSSAForm(controlFlowGraph);
                if (settings.shouldPerformOptimizations) {
                    logger.info("Starting applying optimizations");
                    performOptimizations(controlFlowGraph);
                }
            }
        }
    }

    protected abstract void performOptimizations(ControlFlowGraph controlFlowGraph);

    protected abstract void buildSSAForm(ControlFlowGraph controlFlowGraph);

    public record OxmaCompilerSettings(
            boolean shouldPlotCFGs,
            boolean shouldPerformOptimizations,
            String cfgPictureRelativePathBase,
            String compiledClassesDestinationDirectory,
            boolean shouldBuildSSAForm)
    {
        public static OxmaCompilerSettings defaultSettings() {
            return new OxmaCompilerSettings(
                    true,
                    true,
                    "cfg",
                    "",
                    true);
        }
    }
}
