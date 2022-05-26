import java.util.List;

import kalina.compiler.OxmaCompiler;
import kalina.compiler.bb.v2.ClassBasicBlock;
import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.optimizations.OptimizationManager;
import kalina.compiler.cfg.optimizations.OptimizationManagerFactory;
import kalina.compiler.cfg.ssa.SSAFormBuilder;

/**
 * @author vlad333rrty
 */
public class DCETestCompiler extends OxmaCompiler {

    @Override
    protected void performOptimizations(List<ClassBasicBlock> classBasicBlocks) {
        SSAFormBuilder formBuilder = new SSAFormBuilder();
        for (var classBb : classBasicBlocks) {
            for (var funBb : classBb.getEntry()) {
                ControlFlowGraph controlFlowGraph = ControlFlowGraph.fromRoot(funBb.getCfgRoot());
                formBuilder.buildSSA(controlFlowGraph);
                OptimizationManager optimizationManager = OptimizationManagerFactory.create(controlFlowGraph);
                optimizationManager.optimize();
            }
        }
    }
}
