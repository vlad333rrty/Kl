package kalina.compiler.cfg.builder.nodes;

import java.util.List;

import kalina.compiler.cfg.bb.BasicBlock;

public class CFGNodeWithBranch extends AbstractCFGNode {
    private final AbstractCFGNode thenNode;
    private final AbstractCFGNode elseNode;

    public CFGNodeWithBranch(
            BasicBlock bb,
            AbstractCFGNode thenNode,
            AbstractCFGNode elseNode)
    {
        super(bb);
        this.thenNode = thenNode;
        this.elseNode = elseNode;
    }

    public List<AbstractCFGNode> getChildren() {
        return getBackEdgeNode().isPresent()
                ? List.of(thenNode, elseNode, getBackEdgeNode().get())
                : List.of(thenNode, elseNode);
    }

    @Override
    public void addChild(AbstractCFGNode node) {
        throw new UnsupportedOperationException();
    }
}
