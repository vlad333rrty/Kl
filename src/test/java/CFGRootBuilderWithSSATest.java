import java.io.IOException;
import java.util.List;

import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.v2.ClassBasicBlock;
import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.builder.CFGBuilder;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.ssa.SSAFormBuilder;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.syntax.parser2.OxmaParser;
import kalina.compiler.syntax.parser2.ParseException;
import kalina.compiler.syntax.scanner.Scanner2;
import kalina.internal.CFGDotGraphConstructor;

/**
 * @author vlad333rrty
 */
public class CFGRootBuilderWithSSATest {
    public AbstractCFGNode run(String outputFilePath) throws ParseException, IOException, CFGConversionException, IncompatibleTypesException {
        OxmaParser parser = new OxmaParser(Scanner2.fromLexerResult(outputFilePath));
        ASTRootNode result = parser.parse();
        CFGBuilder cfgBuilder = new CFGBuilder();
        List<ClassBasicBlock> bbs = cfgBuilder.build(result);

        AbstractCFGNode root = bbs.get(0).getEntry().get(0).getCfgRoot();
        SSAFormBuilder formBuilder = new SSAFormBuilder();
        formBuilder.buildSSA(ControlFlowGraph.fromRoot(root));

        CFGDotGraphConstructor.plotMany(bbs);

        return root;
    }
}
