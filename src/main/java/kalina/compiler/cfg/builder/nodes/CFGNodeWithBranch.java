package kalina.compiler.cfg.builder.nodes;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.bb.BasicBlock;

public class CFGNodeWithBranch extends AbstractCFGNode {
    private final AbstractCFGNode thenNode;
    private final AbstractCFGNode elseNode;
    private Optional<AbstractCFGNode> afterThenElseNode = Optional.empty();

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

    public AbstractCFGNode getThenNode() {
        return thenNode;
    }

    public AbstractCFGNode getElseNode() {
        return elseNode;
    }

    public void setAfterThenElseNode(AbstractCFGNode afterThenElseNode) {
        this.afterThenElseNode = Optional.of(afterThenElseNode);
    }

    public Optional<AbstractCFGNode> getAfterThenElseNode() {
        return afterThenElseNode;
    }
}
