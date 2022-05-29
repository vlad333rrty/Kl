package kalina.compiler.cfg.optimizations.cp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.optimizations.ConstantExpressionDetector;
import kalina.compiler.cfg.optimizations.DuUdNet;
import kalina.compiler.cfg.optimizations.DuUdNetBuilder;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.assign.AssignInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.instructions.v2.fake.FakeAssignInstruction;
import kalina.compiler.instructions.v2.fake.PhiFunInstruction;

/**
 * @author vlad333rrty
 */
public class ConstantPropagationPerformer {
    public void perform(ControlFlowGraph controlFlowGraph) {
        Map<Integer, AbstractCFGNode> idToNode = controlFlowGraph.nodes().stream()
                .collect(Collectors.toMap(
                        AbstractCFGNode::getId,
                        Function.identity()
                ));
        DuUdNet duUdNet = DuUdNetBuilder.buildDuUdNet(controlFlowGraph.root());
        Queue<BlockIdAndInstructionIndex> workList = new ArrayDeque<>(
                controlFlowGraph.nodes().stream()
                        .flatMap(node -> {
                            int blockId = node.getId();
                            List<BlockIdAndInstructionIndex> result = new ArrayList<>();
                            for (int i = 0;i < node.getBasicBlock().getInstructions().size(); i++) {
                                result.add(new BlockIdAndInstructionIndex(blockId, i));
                            }
                            return result.stream();
                        })
                        .toList());
        Set<String> constantValues = new HashSet<>();
        Map<String, Object> irToValue = new HashMap<>();
        while (!workList.isEmpty()) {
            BlockIdAndInstructionIndex blockIdAndInstructionIndex = workList.poll();
            BasicBlock bb = idToNode.get(blockIdAndInstructionIndex.id).getBasicBlock();
            Instruction instruction = bb.getInstructions().get(blockIdAndInstructionIndex.index);
            if (instruction instanceof PhiFunInstruction phiFunInstruction) {
                Instruction modified = visitPhi(phiFunInstruction, constantValues, irToValue);
                bb.getInstructions().set(blockIdAndInstructionIndex.index, modified);
            } else if (instruction instanceof AssignInstruction assignInstruction) {

            } else if (instruction instanceof InitInstruction initInstruction) {

            }
        }
    }

    private void visitBlock(BasicBlock bb) {

    }

    private InitInstruction visitInit(
            InitInstruction initInstruction,
            DuUdNet duUdNet,
            DuUdNet.Use use)
    {
        List<Integer> constantIndices = new ArrayList<>();
        IntStream.range(0, initInstruction.getExpressions().size())
                .forEach(i -> {
                    if (ConstantExpressionDetector.isNumberConstant(initInstruction.getExpressions().get(i))) {
                        constantIndices.add(i);
                    }
                });
        return null;
    }

    private AssignInstruction visitAssign() {
        return null;
    }

    private Instruction visitPhi(
            PhiFunInstruction phiFunInstruction,
            Set<String> constantValues,
            Map<String, Object> irToValue)
    {
        if (phiFunInstruction.getArguments().stream()
                .allMatch(x -> constantValues.contains(x.getSsaVariableInfo().getIR())))
        {
            Object firstVal = irToValue.get(phiFunInstruction.getArguments().get(0).getSsaVariableInfo().getIR());
            for (int i = 1; i < phiFunInstruction.getArguments().size(); i++) {
                Object value = irToValue.get(phiFunInstruction.getArguments().get(i).getSsaVariableInfo().getIR());
                if (!value.equals(firstVal)) {
                    return phiFunInstruction;
                }
            }

            return new FakeAssignInstruction(phiFunInstruction.getLhsIR().getIR(), firstVal);
        }

        return phiFunInstruction;
    }



    private Map<String, LatticeElement> getLatticeCells(ControlFlowGraph controlFlowGraph) {
        final Map<String, LatticeElement> latticeCells = new HashMap<>();
        for (var node : controlFlowGraph.nodes()) {
            for (var instruction : node.getBasicBlock().getInstructions()) {
                if (instruction instanceof AssignInstruction assignInstruction) {
                    assignInstruction.getLhs()
                            .forEach(var -> latticeCells.put(var.getIR(), new LatticeElement()));
                } else if (instruction instanceof InitInstruction initInstruction) {
                    initInstruction.getLhs().getVars()
                            .forEach(var -> latticeCells.put(var.getIR(), new LatticeElement()));
                }
            }
        }
        return latticeCells;
    }

    private Map<Integer, Set<Integer>> getExecFlag(ControlFlowGraph controlFlowGraph) {
        Map<Integer, Set<Integer>> execFlag = new HashMap<>();
        for (var node : controlFlowGraph.nodes()) {
            execFlag.computeIfAbsent(node.getId(), k -> new HashSet<>());
        }
        return execFlag;
    }

    private static record Edge(int from, int to) {}

    private static record BlockIdAndInstructionIndex(int id, int index) {}
}
