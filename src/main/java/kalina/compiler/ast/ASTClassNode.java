package kalina.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;

/**
 * @author vlad333rrty
 */
public class ASTClassNode {
    private final String parentClassName;
    private final String className;
    private final List<ASTMethodNode> methodNodes;
    private final List<ASTFieldNode> fieldNodes;
    private final OxmaFunctionTable oxmaFunctionTable;

    public ASTClassNode(String parentClassName, String className, OxmaFunctionTable oxmaFunctionTable) {
        this.parentClassName = parentClassName;
        this.className = className;
        this.methodNodes = new ArrayList<>();
        this.fieldNodes = new ArrayList<>();
        this.oxmaFunctionTable = oxmaFunctionTable;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public String getClassName() {
        return className;
    }

    public List<ASTMethodNode> getMethodNodes() {
        return methodNodes;
    }

    public List<ASTFieldNode> getFieldNodes() {
        return fieldNodes;
    }

    public void addChild(ASTMethodNode methodNode) {
        methodNodes.add(methodNode);
    }

    public void addChild(ASTFieldNode fieldNode) {
        fieldNodes.add(fieldNode);
    }

    public OxmaFunctionTable getOxmaFunctionTable() {
        return oxmaFunctionTable;
    }

    @Override
    public String toString() {
        return "class " + className;
    }
}
