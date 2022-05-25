package kalina.compiler.cfg.optimizations.dce;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.WithIR;
import kalina.compiler.cfg.optimizations.DuUdNet;
import kalina.compiler.cfg.optimizations.DuUdNetBuilder;
import kalina.compiler.cfg.optimizations.ExpressionUnwrapper;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.instructions.FunEndInstruction;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.FunCallInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.instructions.v2.PhiFunInstruction;
import kalina.compiler.instructions.v2.WithCondition;
import kalina.compiler.instructions.v2.WithExpressions;
import kalina.compiler.instructions.v2.br.DoBlockBeginInstruction;
import kalina.compiler.instructions.v2.br.DoBlockEndInstruction;
import kalina.compiler.instructions.v2.br.ForEntryEndInstruction;
import kalina.compiler.instructions.v2.br.ForHeaderInstruction;
import kalina.compiler.instructions.v2.br.IfCondInstruction;
import kalina.compiler.instructions.v2.br.IfElseEndInstruction;
import kalina.compiler.instructions.v2.br.IfThenEndInstruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class DeadCodeEliminator {
    private static final Logger logger = LogManager.getLogger(DeadCodeEliminator.class);
    private static final Set<Class<? extends Instruction>> instructionsNotToBeDeleted = Set.of(
            IfThenEndInstruction.class, IfElseEndInstruction.class, DoBlockBeginInstruction.class,
            DoBlockEndInstruction.class, ForHeaderInstruction.class, ForEntryEndInstruction.class
    );

    public void run(ControlFlowGraph controlFlowGraph) {
        EssentialInstructionsFinder essentialInstructionsFinder =new EssentialInstructionsFinder(
                List.of(FunCallInstruction.class, FunEndInstruction.class, IfCondInstruction.class)
        );
        List<DuUdNet.InstructionCoordinates> essentialInstructions = essentialInstructionsFinder
                .findEssentialInstructions(controlFlowGraph.root());
        Map<Integer, Set<Integer>> blockIdToEssentialInstructions = essentialInstructions.stream()
                .collect(Collectors.toMap(
                        DuUdNet.InstructionCoordinates::blockId,
                        coords -> new HashSet<>(Set.of(coords.instructionIndex())),
                        (oldVal, newVal) -> {
                            oldVal.addAll(newVal);
                            return oldVal;
                        }
                ));
        Map<Integer, BasicBlock> idToBb = controlFlowGraph.nodes().stream()
                .collect(Collectors.toMap(
                        AbstractCFGNode::getId,
                        AbstractCFGNode::getBasicBlock
                ));
        Queue<DuUdNet.InstructionCoordinates> queue = new ArrayDeque<>(essentialInstructions);
        DuUdNet duUdNet = DuUdNetBuilder.buildDuUdNet(controlFlowGraph.root());
        while (!queue.isEmpty()) {
            DuUdNet.InstructionCoordinates coordinates = queue.poll();
            int blockId = coordinates.blockId();
            BasicBlock bb = idToBb.get(blockId);
            Instruction instruction = getInstruction(bb, coordinates);
            if (instruction instanceof WithExpressions withExpressions) {
                withExpressions.getExpressions()
                        .forEach(expr -> checkDefinitions(expr, coordinates, duUdNet, blockIdToEssentialInstructions, queue));
            }
            if (instruction instanceof AbstractAssignInstruction assignInstruction) {
                assignInstruction.getLhs()
                        .forEach(v -> checkUses(v, coordinates, duUdNet, idToBb, blockIdToEssentialInstructions, queue));
            } else if (instruction instanceof InitInstruction initInstruction) {
                initInstruction.getLhs().getVars()
                        .forEach(v -> checkUses(v, coordinates, duUdNet, idToBb, blockIdToEssentialInstructions, queue));
            }
            if (instruction instanceof PhiFunInstruction phiFunInstruction) {
                phiFunInstruction.getArguments()
                        .forEach(expr -> checkDefinitions(expr, coordinates, duUdNet, blockIdToEssentialInstructions, queue));
                checkUses(phiFunInstruction.getLhsIR(), coordinates, duUdNet, idToBb, blockIdToEssentialInstructions, queue);
            }
        }

        logger.info(blockIdToEssentialInstructions);
        for (var node : controlFlowGraph.nodes()) {
            BasicBlock basicBlock = node.getBasicBlock();
            int id = basicBlock.getId();
            int offset = basicBlock.getVarInfoToPhiFun().size();
            List<Instruction> finalInstructions = new ArrayList<>();
            List<Instruction> instructions = basicBlock.getInstructions();
            for (int i = 0, instructionsSize = instructions.size(); i < instructionsSize; i++) {
                Instruction instruction = instructions.get(i);
                Set<Integer> usefulInstructions = blockIdToEssentialInstructions.get(id);
                if (instructionsNotToBeDeleted.contains(instruction.getClass())
                        || (usefulInstructions != null && usefulInstructions.contains(i + offset))) {
                        finalInstructions.add(instruction);
                }
            }
            basicBlock.setInstructions(finalInstructions);
        }
    }


    private Instruction getInstruction(BasicBlock basicBlock, DuUdNet.InstructionCoordinates coordinates) {
        int offset = basicBlock.getVarInfoToPhiFun().size();
        int index = coordinates.instructionIndex() - offset;
        if (index < 0) {
            int i = 0;
            for (var entry : basicBlock.getVarInfoToPhiFun().entrySet()) {
                if (i == coordinates.instructionIndex()) {
                    var expr = entry.getValue().getArguments().stream()
                            .map(x -> new VariableExpression(0, null, x.getName(), x.getCfgIndex())).toList();
                    return new PhiFunInstruction(expr, entry.getKey().getIR());
                }
            }
        }
        return basicBlock.getInstructions().get(coordinates.instructionIndex() - offset);
    }

    private void checkUses(
            WithIR variable,
            DuUdNet.InstructionCoordinates coordinates,
            DuUdNet duUdNet,
            Map<Integer, BasicBlock> idToBb,
            Map<Integer, Set<Integer>> blockIdToEssentialInstructions,
            Queue<DuUdNet.InstructionCoordinates> queue)
    {
        var def = new DuUdNet.Definition(variable.getIR(), coordinates.blockId(), coordinates.instructionIndex());
        List<DuUdNet.InstructionCoordinates> uses = duUdNet.getDuChainProvider().apply(def);
        for (var use : uses) {
            BasicBlock basicBlock = idToBb.get(use.blockId());
            Instruction inst = basicBlock.getInstructions().get(use.instructionIndex());
            if (inst instanceof WithCondition) {
                if (blockIdToEssentialInstructions
                        .computeIfAbsent(def.blockId(), k -> new HashSet<>())
                        .add(use.instructionIndex()))
                {
                    queue.add(use);
                }
            }
        }
    }

    private void checkDefinitions(
            Expression expression,
            DuUdNet.InstructionCoordinates coordinates,
            DuUdNet duUdNet,
            Map<Integer, Set<Integer>> blockIdToEssentialInstructions,
            Queue<DuUdNet.InstructionCoordinates> queue)
    {
        ExpressionUnwrapper
                .unwrapExpression(expression, ve -> {
                    var use = new DuUdNet.Use(ve.getSsaVariableInfo().getIR(), coordinates.blockId(), coordinates.instructionIndex());
                    List<DuUdNet.InstructionCoordinates> defs = duUdNet.getUdChainProvider().apply(use);
                    for (var def : defs) {
                        if (blockIdToEssentialInstructions
                                .computeIfAbsent(def.blockId(), k -> new HashSet<>())
                                .add(def.instructionIndex()))
                        {
                            queue.add(def);
                        }
                    }
                });
    }
}
