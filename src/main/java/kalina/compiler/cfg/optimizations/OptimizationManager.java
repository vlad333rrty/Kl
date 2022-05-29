package kalina.compiler.cfg.optimizations;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.optimizations.cf.ConstantFoldingPerformer;
import kalina.compiler.cfg.optimizations.dce.DeadCodeEliminator;

/**
 * @author vlad333rrty
 */
public class OptimizationManager {
    private final ControlFlowGraph controlFlowGraph;
    private final DeadCodeEliminator deadCodeEliminator;
    private final ConstantFoldingPerformer constantFoldingPerformer;

    public OptimizationManager(
            ControlFlowGraph controlFlowGraph,
            DeadCodeEliminator deadCodeEliminator,
            ConstantFoldingPerformer constantFoldingPerformer)
    {
        this.controlFlowGraph = controlFlowGraph;
        this.deadCodeEliminator = deadCodeEliminator;
        this.constantFoldingPerformer = constantFoldingPerformer;
    }

    public void optimize() {
        constantFoldingPerformer.perform(controlFlowGraph);
        deadCodeEliminator.run(controlFlowGraph);
    }
}
