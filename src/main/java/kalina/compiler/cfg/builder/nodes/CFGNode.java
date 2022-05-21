package kalina.compiler.cfg.builder.nodes;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.cfg.bb.BasicBlock;

/**
 * @author vlad333rrty
 */
public class CFGNode extends AbstractCFGNode {
    private final List<AbstractCFGNode> childrenAndBackEdgeNodes;

    public CFGNode(BasicBlock bb) {
        super(bb);
        this.childrenAndBackEdgeNodes = new ArrayList<>();
    }

    @Override
    public List<? extends AbstractCFGNode> getChildren() {
        return childrenAndBackEdgeNodes;
    }

    @Override
    public void setBackEdgeNode(AbstractCFGNode backEdgeNode) {
        super.setBackEdgeNode(backEdgeNode);
        childrenAndBackEdgeNodes.add(backEdgeNode);
    }

    @Override
    public void addChild(AbstractCFGNode node) {
        childrenAndBackEdgeNodes.add(node);
    }
}
