package kalina.compiler.cfg;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.bb.RootBasicBlock;
import kalina.compiler.cfg.data.RuntimeConstantPool;
import kalina.compiler.instructions.DefaultConstructorInstruction;

/**
 * @author vlad333rrty
 */
public class CfgBuilder {
    public RootBasicBlock build(ASTRootNode root) {

        return null;
    }

    private ClassBasicBlock build(ASTClassNode classNode) {
        RuntimeConstantPool runtimeConstantPool =
                new RuntimeConstantPool(classNode.getClassName(), classNode.getOxmaFunctionTable());
        DefaultConstructorInstruction defaultConstructorInstruction =
                new DefaultConstructorInstruction(runtimeConstantPool.className());
        ClassBasicBlock classBasicBlock = new ClassBasicBlock(defaultConstructorInstruction);
        return null;
    }
}
