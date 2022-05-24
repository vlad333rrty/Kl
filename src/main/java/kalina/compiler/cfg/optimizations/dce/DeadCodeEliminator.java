package kalina.compiler.cfg.optimizations.dce;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.optimizations.DuUdNet;
import kalina.compiler.cfg.optimizations.DuUdNetBuilder;

/**
 * @author vlad333rrty
 */
public class DeadCodeEliminator {
    public void test(AbstractCFGNode root) {
        DuUdNet net = DuUdNetBuilder.buildDuUdNet(root);
        int x;
    }
}
