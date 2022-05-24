package kalina.compiler.cfg.optimizations;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.optimizations.dce.DeadCodeEliminator;

/**
 * @author vlad333rrty
 */
public class OptimizationManager {
    private final AbstractCFGNode root;
    private final DeadCodeEliminator deadCodeEliminator;

    public OptimizationManager(AbstractCFGNode root, DeadCodeEliminator deadCodeEliminator) {
        this.root = root;
        this.deadCodeEliminator = deadCodeEliminator;
    }

    public void optimize() {
        deadCodeEliminator.test(root);
    }
}
