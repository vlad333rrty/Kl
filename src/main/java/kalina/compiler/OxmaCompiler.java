package kalina.compiler;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    protected final Settings settings;

    public OxmaCompiler() {
        this.settings = new Settings.Builder().build();
    }

    public OxmaCompiler(Settings settings) {
        this.settings = settings;
    }

    public final void run(String outputFilePath)
            throws IOException, ParseException, CFGConversionException, CodeGenException, IncompatibleTypesException
    {
        OxmaParser parser = new OxmaParser(new Scanner(outputFilePath));
        ASTRootNode result = parser.parse();
        CFGBuilder cfgBuilder = new CFGBuilder();
        List<ClassBasicBlock> classBasicBlocks = cfgBuilder.build(result);

        if (settings.shouldBuildSSAForm || settings.shouldPerformOptimizations) {
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
                        constructOutputPath(res.getClassName()),
                        res.getByteCode()
                );
            }
        }
    }

    private String constructOutputPath(String className) {
        if (settings.compiledClassesDestinationDirectory.isEmpty()) {
            return className + ".class";
        }
        return settings.compiledClassesDestinationDirectory + "/" + className + ".class";
    }

    private void performCodeTransformations(List<ClassBasicBlock> classBasicBlocks) {
        for (var classBb : classBasicBlocks) {
            for (var funBb : classBb.getEntry()) {
                ControlFlowGraph controlFlowGraph = ControlFlowGraph.fromRoot(funBb.getCfgRoot());
                if (settings.shouldBuildSSAForm) {
                    logger.info("Starting building SSA form");
                    buildSSAForm(controlFlowGraph);
                }
                if (settings.shouldPerformOptimizations) {
                    logger.info("Starting applying optimizations");
                    performOptimizations(controlFlowGraph);
                }
            }
        }
    }

    protected abstract void performOptimizations(ControlFlowGraph controlFlowGraph);

    protected abstract void buildSSAForm(ControlFlowGraph controlFlowGraph);

    public record Settings(
            boolean shouldPlotCFGs,
            String cfgPictureRelativePathBase,
            String compiledClassesDestinationDirectory,
            boolean shouldBuildSSAForm,
            boolean shouldPerformOptimizations)
    {
        public static class Builder {
            private boolean shouldPlotCFGs;
            private String cfgPictureRelativePathBase;
            private String compiledClassesDestinationDirectory;
            private boolean shouldBuildSSAForm;
            private boolean shouldPerformOptimizations;

            public Builder() {
                this.shouldPlotCFGs = true;
                this.cfgPictureRelativePathBase = "cfg";
                this.compiledClassesDestinationDirectory = "";
                this.shouldBuildSSAForm = true;
                this.shouldPerformOptimizations = false;
            }

            public Builder setPlotCfg(boolean shouldPlotCFGs) {
                this.shouldPlotCFGs = shouldPlotCFGs;
                return this;
            }

            public Builder setCfgPictureRelativePathBase(String cfgPictureRelativePathBase) {
                this.cfgPictureRelativePathBase = cfgPictureRelativePathBase;
                return this;
            }

            public Builder setCompiledClassesDestinationDirectory(String compiledClassesDestinationDirectory) {
                this.compiledClassesDestinationDirectory = compiledClassesDestinationDirectory;
                return this;
            }

            public Builder setShouldBuildSSAForm(boolean shouldBuildSSAForm) {
                this.shouldBuildSSAForm = shouldBuildSSAForm;
                return this;
            }

            public Builder setShouldPerformOptimizations(boolean shouldPerformOptimizations) {
                this.shouldPerformOptimizations = shouldPerformOptimizations;
                return this;
            }

            public Settings build() {
                return new Settings(
                        shouldPlotCFGs,
                        cfgPictureRelativePathBase,
                        compiledClassesDestinationDirectory,
                        shouldBuildSSAForm,
                        shouldPerformOptimizations
                );
            }
        }
    }

    public static class SettingsParser {

        public static Settings parseCommandLineArgs(String[] args) {
            Settings.Builder builder = new Settings.Builder();
            for (String arg : args) {
                if (arg.equals("use_optimizations")) {
                    builder.setShouldPerformOptimizations(true);
                } else {
                    Pattern pattern = Pattern.compile("outfile=(.+)");
                    Matcher matcher = pattern.matcher(arg);
                    if (matcher.find()) {
                        String outFile = matcher.group();
                        builder.setCompiledClassesDestinationDirectory(outFile);
                    } else {
                        logger.warn("Unexpected flag: {}", arg);
                    }
                }
            }
            return builder.build();
        }
    }
}
