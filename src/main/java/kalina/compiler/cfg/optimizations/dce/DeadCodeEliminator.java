package kalina.compiler.cfg.optimizations.dce;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.AssignArrayVariableInfo;
import kalina.compiler.cfg.data.WithIR;
import kalina.compiler.cfg.optimizations.DuUdNet;
import kalina.compiler.cfg.optimizations.DuUdNetBuilder;
import kalina.compiler.cfg.optimizations.ExpressionUnwrapper;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.ArrayElementAssignExpression;
import kalina.compiler.instructions.FunEndInstruction;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.ClassPropertyCallChainInstruction;
import kalina.compiler.instructions.v2.FunCallInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.instructions.v2.WithCondition;
import kalina.compiler.instructions.v2.WithExpressions;
import kalina.compiler.instructions.v2.assign.ArrayElementAssign;
import kalina.compiler.instructions.v2.assign.ArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.assign.AssignInstruction;
import kalina.compiler.instructions.v2.assign.FieldArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.assign.FieldAssignInstruction;
import kalina.compiler.instructions.v2.br.DoBlockBeginInstruction;
import kalina.compiler.instructions.v2.br.DoBlockEndInstruction;
import kalina.compiler.instructions.v2.br.IfCondInstruction;
import kalina.compiler.instructions.v2.br.IfElseEndInstruction;
import kalina.compiler.instructions.v2.br.IfThenEndInstruction;
import kalina.compiler.instructions.v2.br._for.ForCondInstruction;
import kalina.compiler.instructions.v2.br._for.ForEntryEndInstruction;
import kalina.compiler.instructions.v2.fake.FunArgsInitInstruction;
import kalina.compiler.instructions.v2.fake.PhiFunInstruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class DeadCodeEliminator {
    private static final Logger logger = LogManager.getLogger(DeadCodeEliminator.class);
    private static final Set<Class<? extends Instruction>> instructionsNotToBeDeleted = Set.of(
            FunArgsInitInstruction.class
    );

    public void run(ControlFlowGraph controlFlowGraph) {
        EssentialInstructionsFinder essentialInstructionsFinder =new EssentialInstructionsFinder(
                List.of(
                        FunCallInstruction.class, FunEndInstruction.class, IfCondInstruction.class,
                        IfThenEndInstruction.class, IfElseEndInstruction.class, DoBlockBeginInstruction.class,
                        DoBlockEndInstruction.class, ForCondInstruction.class, ForEntryEndInstruction.class,
                        ArrayElementAssignInstruction.class, ClassPropertyCallChainInstruction.class,
                        FieldAssignInstruction.class, FieldArrayElementAssignInstruction.class
                )
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
                        .forEach(expr -> checkDefinitionsForUse(expr, coordinates, duUdNet, blockIdToEssentialInstructions, queue));
            }
            if (instruction instanceof WithCondition withCondition) {
                checkDefinitionsForUse(withCondition.getCondExpression(), coordinates, duUdNet, blockIdToEssentialInstructions, queue);
            }
            if (instruction instanceof AssignInstruction assignInstruction) {
                assignInstruction.getLhs()
                        .forEach(v -> checkUsesForDefinition(v, coordinates, duUdNet, idToBb, blockIdToEssentialInstructions, queue));
            } else if (instruction instanceof InitInstruction initInstruction) {
                initInstruction.getLhs().getVars()
                        .forEach(v -> checkUsesForDefinition(v, coordinates, duUdNet, idToBb, blockIdToEssentialInstructions, queue));
            }
            if (instruction instanceof PhiFunInstruction phiFunInstruction) {
                phiFunInstruction.getArguments()
                        .forEach(expr -> checkDefinitionsForUse(expr, coordinates, duUdNet, blockIdToEssentialInstructions, queue));
                checkUsesForDefinition(phiFunInstruction.getLhsIR(), coordinates, duUdNet, idToBb, blockIdToEssentialInstructions, queue);
            }
            if (instruction instanceof ArrayElementAssignInstruction arrayElementAssignInstruction) {
                arrayElementAssignInstruction.getLhs().stream()
                        .map(ArrayElementAssignExpression::new)
                        .forEach(expression -> checkDefinitionsForUse(expression, coordinates, duUdNet, blockIdToEssentialInstructions, queue));
            }
            if (instruction instanceof ArrayElementAssign arrayElementAssign) {
                arrayElementAssign.getAssignArrayVariableInfo().stream()
                        .map(AssignArrayVariableInfo::getIndices)
                        .flatMap(Collection::stream)
                        .forEach(expression -> checkDefinitionsForUse(expression, coordinates, duUdNet, blockIdToEssentialInstructions, queue));
            }
        }

        logger.info(blockIdToEssentialInstructions);
        for (var node : controlFlowGraph.nodes()) {
            BasicBlock basicBlock = node.getBasicBlock();
            int id = basicBlock.getId();
            Set<Integer> usefulInstructions = blockIdToEssentialInstructions.get(id);
            int offset = basicBlock.getPhiFunInstructions().size();
            List<PhiFunInstruction> finalPhiInstructions =
                    getFinalInstructions(basicBlock.getPhiFunInstructions(), usefulInstructions, 0);
            basicBlock.setPhiFunInstructions(finalPhiInstructions);
            List<Instruction> finalInstructions =
                    getFinalInstructions(basicBlock.getInstructions(), usefulInstructions, offset);
            basicBlock.setInstructions(finalInstructions);
        }
    }

    private <T> List<T> getFinalInstructions(List<T> instructions, Set<Integer> usefulInstructions, int offset) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            T instruction = instructions.get(i);
            if (instructionsNotToBeDeleted.contains(instruction.getClass())
                    || (usefulInstructions != null && usefulInstructions.contains(i + offset))) {
                result.add(instruction);
            }
        }
        return result;
    }

    private Instruction getInstruction(BasicBlock basicBlock, DuUdNet.InstructionCoordinates coordinates) {
        int offset = basicBlock.getPhiFunInstructions().size();
        int index = coordinates.instructionIndex() - offset;
        if (index < 0) {
            return basicBlock.getPhiFunInstructions().get(coordinates.instructionIndex());
        }
        return basicBlock.getInstructions().get(index);
    }

    private void checkUsesForDefinition(
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
            final Instruction inst;
            if (use.instructionIndex() < basicBlock.getInstructions().size()) {
                inst = basicBlock.getInstructions().get(use.instructionIndex());
            } else {
                int offset = basicBlock.getPhiFunInstructions().size();
                if (use.instructionIndex() < offset) {
                    inst = basicBlock.getPhiFunInstructions().get(use.instructionIndex());
                } else {
                    inst = basicBlock.getInstructions().get(use.instructionIndex() - offset);
                }
            }
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

    private void checkDefinitionsForUse(
            Expression expression,
            DuUdNet.InstructionCoordinates coordinates,
            DuUdNet duUdNet,
            Map<Integer, Set<Integer>> blockIdToEssentialInstructions,
            Queue<DuUdNet.InstructionCoordinates> queue)
    {
        ExpressionUnwrapper.unwrapExpression(
                expression,
                ve -> saveEssentialDefinitions(ve, coordinates, duUdNet, blockIdToEssentialInstructions, queue));
    }

    private void saveEssentialDefinitions(
            WithIR withIR,
            DuUdNet.InstructionCoordinates coordinates,
            DuUdNet duUdNet,
            Map<Integer, Set<Integer>> blockIdToEssentialInstructions,
            Queue<DuUdNet.InstructionCoordinates> queue)
    {
        var use = new DuUdNet.Use(withIR.getIR(), coordinates.blockId(), coordinates.instructionIndex());
        List<DuUdNet.InstructionCoordinates> defs = duUdNet.getUdChainProvider().apply(use);
        for (var def : defs) {
            if (blockIdToEssentialInstructions
                    .computeIfAbsent(def.blockId(), k -> new HashSet<>())
                    .add(def.instructionIndex()))
            {
                queue.add(def);
            }
        }
    }
}
