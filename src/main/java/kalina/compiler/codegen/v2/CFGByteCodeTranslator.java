package kalina.compiler.codegen.v2;

import java.util.Optional;

import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.builder.nodes.CFGNode;
import kalina.compiler.cfg.builder.nodes.CFGNodeWithBranch;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class CFGByteCodeTranslator {
    public void translateCFGToByteCode(AbstractCFGNode root, MethodVisitor mv) throws CodeGenException {
        BasicBlock bb = root.getBasicBlock();
        for (Instruction instruction : bb.getInstructions()) {
            instruction.translateToBytecode(Optional.of(mv), Optional.empty());
        }
        if (root instanceof CFGNodeWithBranch node) {
            translateCFGToByteCode(node.getThenNode(), mv);
            translateCFGToByteCode(node.getElseNode(), mv);
            if (node.getAfterThenElseNode().isPresent()) {
                translateCFGToByteCode(node.getAfterThenElseNode().get(), mv);
            }
        } else if (root instanceof CFGNode node) {
            if (node.getNext().isPresent()) {
                translateCFGToByteCode(node.getNext().get(), mv);
            }
        } else {
            throw new IllegalArgumentException("Unexpected cfg node: " + root);
        }
    }
}
