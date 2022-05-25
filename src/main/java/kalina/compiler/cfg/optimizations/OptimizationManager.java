package kalina.compiler.cfg.optimizations;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.optimizations.dce.DeadCodeEliminator;

/**
 * @author vlad333rrty
 */
public class OptimizationManager {
    private final ControlFlowGraph controlFlowGraph;
    private final DeadCodeEliminator deadCodeEliminator;

    public OptimizationManager(ControlFlowGraph controlFlowGraph, DeadCodeEliminator deadCodeEliminator) {
        this.controlFlowGraph = controlFlowGraph;
        this.deadCodeEliminator = deadCodeEliminator;
    }

    public void optimize() {
        deadCodeEliminator.run(controlFlowGraph);
    }
}
