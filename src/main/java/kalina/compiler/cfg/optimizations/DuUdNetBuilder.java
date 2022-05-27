package kalina.compiler.cfg.optimizations;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithCondition;
import kalina.compiler.instructions.v2.WithExpressions;
import kalina.compiler.instructions.v2.WithLHS;

/**
 * @author vlad333rrty
 */
public class DuUdNetBuilder {
    public static DuUdNet buildDuUdNet(AbstractCFGNode root) {
        Map<String, DuUdNet.Definition> nameToDefinition = DefinitionMetaProvider.getNameToDefinition(root);
        Map<DuUdNet.Definition, List<DuUdNet.InstructionCoordinates>> defToDuChain = new HashMap<>();
        for (var entry : nameToDefinition.entrySet()) {
            defToDuChain.put(entry.getValue(), new ArrayList<>());
        }
        Map<DuUdNet.Use, List<DuUdNet.InstructionCoordinates>> useToUdChain = new HashMap<>();
        DuUdChainEnricher enricher = new DuUdChainEnricher(nameToDefinition, defToDuChain, useToUdChain);
        fillDuUdChains(root, enricher, new HashSet<>());

        return new DuUdNet(defToDuChain, useToUdChain);
    }

    private static void fillDuUdChains(
            AbstractCFGNode node,
            DuUdChainEnricher enricher,
            Set<Integer> usedNodes)
    {
        fillDuUdChainsInt(node, enricher);
        usedNodes.add(node.getId());
        for (var child : node.getChildren()) {
            if (!usedNodes.contains(child.getId())) {
                fillDuUdChains(child, enricher, usedNodes);
            }
        }
    }

    private static void fillDuUdChainsInt(
            AbstractCFGNode node,
            DuUdChainEnricher duUdChainEnricher)
    {
        List<Instruction> instructions = node.getBasicBlock().getInstructions();
        int j = 0;
        for (var phiFunInstruction : node.getBasicBlock().getPhiFunInstructions()) {
            var du = new DuUdNet.InstructionCoordinates(node.getId(), j++);
            phiFunInstruction.getArguments().forEach(x -> duUdChainEnricher.putForIR(x.getSsaVariableInfo(), du));
        }
        int offset = node.getBasicBlock().getPhiFunInstructions().size();
        for (int i = 0, instructionsSize = instructions.size(); i < instructionsSize; i++) {
            Instruction instruction = instructions.get(i);
            var du = new DuUdNet.InstructionCoordinates(node.getId(), i + offset);
            if (instruction instanceof WithExpressions withExpressions) {
                withExpressions.getExpressions()
                        .forEach(x -> duUdChainEnricher.putForExpression(x, du));
            } else if (instruction instanceof WithCondition withCondition) {
                duUdChainEnricher.putForExpression(withCondition.getCondExpression(), du);
            }
        }
    }

    private static class DuUdChainEnricher {
        private final Map<String, DuUdNet.Definition> nameToDefinition;
        private final Map<DuUdNet.Definition, List<DuUdNet.InstructionCoordinates>> defToDuChain;
        private final Map<DuUdNet.Use, List<DuUdNet.InstructionCoordinates>> useToUdChain;

        public DuUdChainEnricher(
                Map<String, DuUdNet.Definition> nameToDefinition,
                Map<DuUdNet.Definition, List<DuUdNet.InstructionCoordinates>> defToDuChain,
                Map<DuUdNet.Use, List<DuUdNet.InstructionCoordinates>> useToUdChain)
        {
            this.nameToDefinition = nameToDefinition;
            this.defToDuChain = defToDuChain;
            this.useToUdChain = useToUdChain;
        }

        public void putForIR(SSAVariableInfo ssaVariableInfo, DuUdNet.InstructionCoordinates instructionCoordinates) {
            var def = nameToDefinition.get(ssaVariableInfo.getIR());
            putDuUdChains(def, instructionCoordinates);
        }

        public void putForExpression(Expression expression, DuUdNet.InstructionCoordinates instructionCoordinates) {
            ExpressionUnwrapper.unwrapExpression(expression, ve -> {
                DuUdNet.Definition def = nameToDefinition.get(ve.getIR());
                putDuUdChains(def, instructionCoordinates);
            });
        }

        private void putDuUdChains(DuUdNet.Definition definition, DuUdNet.InstructionCoordinates instructionCoordinates) {
            defToDuChain.get(definition).add(instructionCoordinates);
            var use = new DuUdNet.Use(definition.varName(), instructionCoordinates.blockId(), instructionCoordinates.instructionIndex());
            useToUdChain.computeIfAbsent(use, k -> new ArrayList<>())
                    .add(new DuUdNet.InstructionCoordinates(definition.blockId(), definition.instructionIndex()));
        }
    }

    private static class DefinitionMetaProvider {
        public static Map<String, DuUdNet.Definition> getNameToDefinition(AbstractCFGNode root) {
            Map<String, BlockIdAndInstructionIndex> nameToDefinitionMeta = new HashMap<>();
            fillNameToDefinitionMetaInt(root, new HashSet<>(), nameToDefinitionMeta);
            return nameToDefinitionMeta.entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(
                            entry.getKey(),
                            new DuUdNet.Definition(
                                    entry.getKey(),
                                    entry.getValue().blockId,
                                    entry.getValue().instructionIndex))
                    ).collect(Collectors.toMap(
                            AbstractMap.SimpleEntry::getKey,
                            AbstractMap.SimpleEntry::getValue
                    ));
        }

        private static void fillNameToDefinitionMetaInt(
                AbstractCFGNode node,
                Set<Integer> usedNodes,
                Map<String, BlockIdAndInstructionIndex> nameToDefinitionMeta)
        {
            usedNodes.add(node.getId());
            BasicBlock bb = node.getBasicBlock();
            List<Instruction> instructions = bb.getInstructions();
            int blockId = bb.getId();
            int j = 0;
            for (var phiFunInstruction : bb.getPhiFunInstructions()) {
                nameToDefinitionMeta.put(phiFunInstruction.getLhsIR().getIR(), new BlockIdAndInstructionIndex(blockId, j++));
            }
            final int offset = bb.getPhiFunInstructions().size();
            for (int i = 0, instructionsSize = instructions.size(); i < instructionsSize; i++) {
                Instruction instruction = instructions.get(i);
                final int finalI = i + offset;
                traverseInstruction(instruction, nameToDefinitionMeta, blockId, finalI);
            }

            for (var child : node.getChildren()) {
                if (!usedNodes.contains(child.getId())) {
                    fillNameToDefinitionMetaInt(child, usedNodes, nameToDefinitionMeta);
                }
            }
        }

        private static void traverseInstruction(
                Instruction instruction,
                Map<String, BlockIdAndInstructionIndex> nameToDefinitionMeta,
                int blockId,
                int instructionIndex)
        {
            if (instruction instanceof WithLHS withLHS) {
                withLHS.getVariableInfos()
                        .forEach(var -> nameToDefinitionMeta
                                .put(
                                        var.getIR(),
                                        new BlockIdAndInstructionIndex(blockId, instructionIndex)
                                )
                        );
            }
        }
    }

    private static record BlockIdAndInstructionIndex(int blockId, int instructionIndex) {}
}
