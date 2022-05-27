package kalina.compiler.cfg.builder.nodes;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.bb.BasicBlock;

/**
 * @author vlad333rrty
 */
public abstract class AbstractCFGNode {
    private final BasicBlock bb;
    private Optional<AbstractCFGNode> backEdgeNode;

    public AbstractCFGNode(BasicBlock bb) {
        this.bb = bb;
        this.backEdgeNode = Optional.empty();
    }

    public BasicBlock getBasicBlock() {
        return bb;
    }

    public Optional<AbstractCFGNode> getBackEdgeNode() {
        return backEdgeNode;
    }

    public void setBackEdgeNode(AbstractCFGNode backEdgeNode) {
        this.backEdgeNode = Optional.of(backEdgeNode);
        backEdgeNode.addAncestor(this);
    }

    public int getId() {
        return getBasicBlock().getId();
    }

    public abstract List<? extends AbstractCFGNode> getChildren();

    public abstract void addChild(AbstractCFGNode node);

    public abstract List<AbstractCFGNode> getAncestors();

    public abstract void addAncestor(AbstractCFGNode node);
}
