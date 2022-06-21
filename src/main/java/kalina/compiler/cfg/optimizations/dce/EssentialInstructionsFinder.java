package kalina.compiler.cfg.optimizations.dce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.optimizations.DuUdNet;
import kalina.compiler.instructions.Instruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
class EssentialInstructionsFinder {
    private static final Logger logger = LogManager.getLogger(EssentialInstructionsFinder.class);

    private final List<Class<? extends Instruction>> essentialInstructions;

    public EssentialInstructionsFinder(List<Class<? extends Instruction>> essentialInstructions) {
        this.essentialInstructions = essentialInstructions;
    }

    public List<DuUdNet.InstructionCoordinates> findEssentialInstructions(AbstractCFGNode root) {
        Set<Integer> usedNodes = new HashSet<>();
        Stack<AbstractCFGNode> stack = new Stack<>();
        stack.add(root);
        List<DuUdNet.InstructionCoordinates> essentialInstructions = new ArrayList<>();
        while (!stack.isEmpty()) {
            AbstractCFGNode node = stack.pop();
            usedNodes.add(node.getId());
            essentialInstructions.addAll(findBlockEssentialInstructions(node));
            node.getChildren().stream()
                    .filter(child -> !usedNodes.contains(child.getId()))
                    .forEach(stack::push);
        }
        return essentialInstructions;
    }

    private List<DuUdNet.InstructionCoordinates> findBlockEssentialInstructions(AbstractCFGNode node) {
        int nodeId = node.getId();
        List<DuUdNet.InstructionCoordinates> blockEssentialInstructions = new ArrayList<>();
        List<Instruction> instructions = node.getBasicBlock().getInstructions();
        int offset = node.getBasicBlock().getPhiFunInstructions().size();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            int finalI = i + offset;
            if (essentialInstructions.stream().anyMatch(x -> x.isInstance(instruction))) {
                blockEssentialInstructions.add(new DuUdNet.InstructionCoordinates(nodeId, finalI));
            } else if (DangerousInstructionsDetector.isDangerousInstruction(instruction)) {
                logger.info("Instruction {} is dangerous", instruction);
                blockEssentialInstructions.add(new DuUdNet.InstructionCoordinates(nodeId, finalI));
            }
        }

        return blockEssentialInstructions;
    }
}
