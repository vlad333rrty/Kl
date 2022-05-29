package kalina.compiler.cfg.builder;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTFieldNode;
import kalina.compiler.bb.v2.FieldBasicBlock;
import kalina.compiler.instructions.v2.FieldInitInstruction;

/**
 * @author vlad333rrty
 */
public class FieldCFGBuilder {
    public List<FieldBasicBlock> build(ASTClassNode classNode) {
        List<FieldBasicBlock> result = new ArrayList<>();
        for (ASTFieldNode node : classNode.getFieldNodes()) {
            FieldInitInstruction fieldInitInstruction = new FieldInitInstruction(
                    node.getName(),
                    node.getType(),
                    node.getAccessModifier(),
                    node.getModifiers());
            result.add(new FieldBasicBlock(fieldInitInstruction));
        }

        return result;
    }
}
