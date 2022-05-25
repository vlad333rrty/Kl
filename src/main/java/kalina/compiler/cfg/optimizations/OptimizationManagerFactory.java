package kalina.compiler.cfg.optimizations;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.optimizations.dce.DeadCodeEliminator;

/**
 * @author vlad333rrty
 */
public class OptimizationManagerFactory {

    public static OptimizationManager create(ControlFlowGraph controlFlowGraph) {
        return new OptimizationManager(controlFlowGraph, new DeadCodeEliminator());
    }
}
