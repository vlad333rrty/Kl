package kalina.compiler.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;

/**
 * @author vlad333rrty
 */
public record ControlFlowGraph(AbstractCFGNode root, List<AbstractCFGNode> nodes) {
    public static ControlFlowGraph fromRoot(AbstractCFGNode root) {
        return new ControlFlowGraph(root, gatherNodes(root));
    }

    private static List<AbstractCFGNode> gatherNodes(AbstractCFGNode root) {
        Set<Integer> traversedNodes = new HashSet<>();
        Stack<AbstractCFGNode> stack = new Stack<>();
        stack.push(root);
        List<AbstractCFGNode> nodes = new ArrayList<>();
        while (!stack.isEmpty()) {
            AbstractCFGNode v = stack.pop();
            nodes.add(v);
            traversedNodes.add(v.getId());
            v.getChildren().stream()
                    .filter(child -> !traversedNodes.contains(child.getId()))
                    .forEach(stack::push);
        }

        return nodes;
    }
}
