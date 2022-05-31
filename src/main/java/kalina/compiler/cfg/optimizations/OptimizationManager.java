package kalina.compiler.cfg.optimizations;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.optimizations.cf.ConstantFoldingPerformer;
import kalina.compiler.cfg.optimizations.cp.ConstantPropagationPerformer;
import kalina.compiler.cfg.optimizations.dce.DeadCodeEliminator;

/**
 * @author vlad333rrty
 */
public class OptimizationManager {
    private final ControlFlowGraph controlFlowGraph;
    private final DeadCodeEliminator deadCodeEliminator;
    private final ConstantFoldingPerformer constantFoldingPerformer;
    private final ConstantPropagationPerformer constantPropagationPerformer;

    public OptimizationManager(
            ControlFlowGraph controlFlowGraph,
            DeadCodeEliminator deadCodeEliminator,
            ConstantFoldingPerformer constantFoldingPerformer,
            ConstantPropagationPerformer constantPropagationPerformer)
    {
        this.controlFlowGraph = controlFlowGraph;
        this.deadCodeEliminator = deadCodeEliminator;
        this.constantFoldingPerformer = constantFoldingPerformer;
        this.constantPropagationPerformer = constantPropagationPerformer;
    }

    public void optimize() {
        constantFoldingPerformer.perform(controlFlowGraph);
        constantPropagationPerformer.perform(controlFlowGraph);
        deadCodeEliminator.run(controlFlowGraph);
    }
}
