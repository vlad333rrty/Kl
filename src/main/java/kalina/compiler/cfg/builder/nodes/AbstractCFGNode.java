package kalina.compiler.cfg.builder.nodes;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.bb.BasicBlock;

/**
 * @author vlad333rrty
 */
public abstract class AbstractCFGNode {
    private final BasicBlock bb;
    private Optional<AbstractCFGNode> backEdgeNode = Optional.empty();
    private boolean mark;

    public AbstractCFGNode(BasicBlock bb) {
        this.bb = bb;
    }

    public boolean isMarked() {
        return mark;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public BasicBlock getBasicBlock() {
        return bb;
    }

    public Optional<AbstractCFGNode> getBackEdgeNode() {
        return backEdgeNode;
    }

    public void setBackEdgeNode(AbstractCFGNode backEdgeNode) {
        this.backEdgeNode = Optional.of(backEdgeNode);
    }

    public int getId() {
        return getBasicBlock().getId();
    }

    public abstract List<? extends AbstractCFGNode> getChildren();

    public abstract void addChild(AbstractCFGNode node);
}
