package kalina.compiler.cfg.builder.items;

import java.util.Optional;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.builder.nodes.CFGNodeWithBranch;

/**
 * @author vlad333rrty
 */
public record ThenAndElseNodes(AbstractCFGNode thenNode, AbstractCFGNode elseNode, Optional<CFGNodeWithBranch> condNode) {
}
