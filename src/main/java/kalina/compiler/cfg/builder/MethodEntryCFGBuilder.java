package kalina.compiler.cfg.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class MethodEntryCFGBuilder {
    private static final Logger logger = LogManager.getLogger(MethodEntryCFGBuilder.class);

    private final MethodEntryCFGTraverser traverser;

    public MethodEntryCFGBuilder(MethodEntryCFGTraverser traverser) {
        this.traverser = traverser;
    }

    public AbstractCFGNode build(
            List<ASTExpression> methodEntry,
            AbstractLocalVariableTable localVariableTable,
            List<String> funArgsNames) throws CFGConversionException, IncompatibleTypesException
    {
        var root = traverser.traverse(methodEntry.iterator(), localVariableTable, funArgsNames);
        indexNodesInTraverseOrder(root);
        return root;
    }

    private void indexNodesInTraverseOrder(AbstractCFGNode root) {
        indexNodesInTraverseOrderInt(root, new HashSet<>(), new Counter());
    }

    private void indexNodesInTraverseOrderInt(
            AbstractCFGNode node,
            Set<Integer> usedNodes,
            Counter counter)
    {
        usedNodes.add(node.getId());
        node.getBasicBlock().setNewId(counter.getNext());
        for (var child : node.getChildren()) {
            if (!usedNodes.contains(child.getBasicBlock().getOldId())) {
                indexNodesInTraverseOrderInt(child, usedNodes, counter);
            }
        }
    }

    private static class Counter {
        private int index;

        public int getNext() {
            return index++;
        }
    }
}
