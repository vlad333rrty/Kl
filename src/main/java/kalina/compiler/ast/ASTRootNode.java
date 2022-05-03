package kalina.compiler.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vlad333rrty
 */
public class ASTRootNode {
    private final List<ASTClassNode> classNodes;

    public ASTRootNode() {
        this.classNodes = new ArrayList<>();
    }

    public List<ASTClassNode> getClassNodes() {
        return classNodes;
    }

    public void addChild(ASTClassNode node) {
        classNodes.add(node);
    }

    @Override
    public String toString() {
        return "ROOT, " + classNodes.size() + " children";
    }
}
