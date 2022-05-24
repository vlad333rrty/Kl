package kalina.compiler.cfg.optimizations;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.optimizations.dce.DeadCodeEliminator;

/**
 * @author vlad333rrty
 */
public class OptimizationManagerFactory {

    public static OptimizationManager create(AbstractCFGNode root) {
        return new OptimizationManager(root, new DeadCodeEliminator());
    }
}
