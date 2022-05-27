package kalina.compiler.cfg.builder.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.bb.BasicBlock;

/**
 * @author vlad333rrty
 */
public class CFGNode extends AbstractCFGNode {
    private final List<AbstractCFGNode> childrenAndBackEdgeNode;
    private final List<AbstractCFGNode> ancestors;
    private Optional<AbstractCFGNode> next = Optional.empty();

    public CFGNode(BasicBlock bb) {
        super(bb);
        this.childrenAndBackEdgeNode = new ArrayList<>();
        this.ancestors = new ArrayList<>();
    }

    private CFGNode(BasicBlock bb, List<AbstractCFGNode> childrenAndBackEdgeNode, List<AbstractCFGNode> ancestors) {
        super(bb);
        this.childrenAndBackEdgeNode = childrenAndBackEdgeNode;
        this.ancestors = ancestors;
    }

    @Override
    public List<? extends AbstractCFGNode> getChildren() {
        return childrenAndBackEdgeNode;
    }

    @Override
    public void setBackEdgeNode(AbstractCFGNode backEdgeNode) {
        super.setBackEdgeNode(backEdgeNode);
        childrenAndBackEdgeNode.add(backEdgeNode);
    }

    @Override
    public void addChild(AbstractCFGNode node) {
        childrenAndBackEdgeNode.add(node);
        node.addAncestor(this);
    }

    @Override
    public List<AbstractCFGNode> getAncestors() {
        return ancestors;
    }

    @Override
    public void addAncestor(AbstractCFGNode node) {
        ancestors.add(node);
    }

    public Optional<AbstractCFGNode> getNext() {
        return next;
    }

    public void setNext(AbstractCFGNode node) {
        this.next = Optional.of(node);
    }
}
