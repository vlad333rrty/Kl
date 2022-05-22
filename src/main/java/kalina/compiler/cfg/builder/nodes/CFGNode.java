package kalina.compiler.cfg.builder.nodes;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.cfg.bb.BasicBlock;

/**
 * @author vlad333rrty
 */
public class CFGNode extends AbstractCFGNode {
    private final List<AbstractCFGNode> childrenAndBackEdgeNode;

    public CFGNode(BasicBlock bb) {
        super(bb);
        this.childrenAndBackEdgeNode = new ArrayList<>();
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
    }
}
