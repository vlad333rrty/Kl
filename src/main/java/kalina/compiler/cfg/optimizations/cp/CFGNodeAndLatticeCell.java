package kalina.compiler.cfg.optimizations.cp;

import java.util.Map;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;

/**
 * @author vlad333rrty
 */
record CFGNodeAndLatticeCell(AbstractCFGNode cfgNode, Map<String, LatticeElement> latCell) {}
