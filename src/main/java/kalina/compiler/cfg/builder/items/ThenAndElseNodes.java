package kalina.compiler.cfg.builder.items;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;

/**
 * @author vlad333rrty
 */
public record ThenAndElseNodes(AbstractCFGNode thenNode, AbstractCFGNode elseNode) {
}
