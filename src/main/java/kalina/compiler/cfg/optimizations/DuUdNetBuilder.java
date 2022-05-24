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
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.instructions.v2.WithCondition;
import kalina.compiler.instructions.v2.WithExpressions;
import kalina.compiler.instructions.v2.br.ForHeaderInstruction;

/**
 * @author vlad333rrty
 */
public class DuUdNetBuilder {
    private static final Map<DuUdNet.Use, List<DuUdNet.DefinitionCoordinates>> useToUdChain = new HashMap<>();

    public static DuUdNet buildDuUdNet(AbstractCFGNode root) {
        Map<String, DuUdNet.Definition> nameToDefinition = DefinitionMetaProvider.getNameToDefinition(root);
        Map<DuUdNet.Definition, List<DuUdNet.UseCoordinates>> defToDuChain = new HashMap<>();
        for (var entry : nameToDefinition.entrySet()) {
            defToDuChain.put(entry.getValue(), new ArrayList<>());
        }
        Map<DuUdNet.Use, List<DuUdNet.DefinitionCoordinates>> useToUdChain = new HashMap<>();
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
        for (var entry : node.getBasicBlock().getVarInfoToPhiFun().entrySet()) {
            var du = new DuUdNet.UseCoordinates(node.getId(), j++);
            entry.getValue().getArguments().forEach(x -> duUdChainEnricher.putForIR(x, du));
        }
        int offset = node.getBasicBlock().getVarInfoToPhiFun().size();
        for (int i = 0, instructionsSize = instructions.size(); i < instructionsSize; i++) {
            Instruction instruction = instructions.get(i);
            var du = new DuUdNet.UseCoordinates(node.getId(), i + offset);
            if (instruction instanceof WithExpressions withExpressions) {
                withExpressions.getExpressions()
                        .forEach(x -> duUdChainEnricher.putForExpression(x, du));
            }
            if (instruction instanceof WithCondition withCondition) {
                withCondition.getCondExpression().getExpressions()
                        .forEach(x -> duUdChainEnricher.putForExpression(x, du));
            }
        }
    }

    private static class DuUdChainEnricher {
        private final Map<String, DuUdNet.Definition> nameToDefinition;
        private final Map<DuUdNet.Definition, List<DuUdNet.UseCoordinates>> defToDuChain;
        private final Map<DuUdNet.Use, List<DuUdNet.DefinitionCoordinates>> useToUdChain;

        public DuUdChainEnricher(
                Map<String, DuUdNet.Definition> nameToDefinition,
                Map<DuUdNet.Definition, List<DuUdNet.UseCoordinates>> defToDuChain,
                Map<DuUdNet.Use, List<DuUdNet.DefinitionCoordinates>> useToUdChain)
        {
            this.nameToDefinition = nameToDefinition;
            this.defToDuChain = defToDuChain;
            this.useToUdChain = useToUdChain;
        }

        public void putForIR(SSAVariableInfo ssaVariableInfo, DuUdNet.UseCoordinates useCoordinates) {
            var def = nameToDefinition.get(ssaVariableInfo.getIR());
            putDuUdChains(def, useCoordinates);
        }

        public void putForExpression(Expression expression, DuUdNet.UseCoordinates useCoordinates) {
            if (expression instanceof VariableExpression variableExpression) {
                DuUdNet.Definition def = nameToDefinition.get(variableExpression.getSsaVariableInfo().getIR());
                putDuUdChains(def, useCoordinates);
            } else if (expression instanceof ArithmeticExpression arithmeticExpression) {
                arithmeticExpression.getTerms().forEach(x -> putForExpression(x, useCoordinates));
            } else if (expression instanceof Term term) {
                term.getFactors().forEach(x -> putForExpression(x, useCoordinates));
            } else if(expression instanceof Factor factor) {
                putForExpression(factor.getExpression(), useCoordinates);
            } else if (expression instanceof AbstractFunCallExpression funCallExpression) {
                funCallExpression.getArguments().forEach(x -> putForExpression(x, useCoordinates));
            } else if (expression instanceof CondExpression condExpression) {
                condExpression.getExpressions().forEach(x -> putForExpression(x, useCoordinates));
            }
        }

        private void putDuUdChains(DuUdNet.Definition definition, DuUdNet.UseCoordinates useCoordinates) {
            defToDuChain.get(definition).add(useCoordinates);
            var use = new DuUdNet.Use(definition.varName(), useCoordinates.blockId(), useCoordinates.instructionIndex());
            useToUdChain.computeIfAbsent(use, k -> new ArrayList<>())
                    .add(new DuUdNet.DefinitionCoordinates(definition.blockId(), definition.instructionIndex()));
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
            for (var entry : bb.getVarInfoToPhiFun().entrySet()) {
                nameToDefinitionMeta.put(entry.getKey().getIR(), new BlockIdAndInstructionIndex(blockId, j++));
            }
            final int offset = bb.getVarInfoToPhiFun().size();
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
            if (instruction instanceof AbstractAssignInstruction assignInstruction) {
                assignInstruction.getLhs()
                        .forEach(var -> nameToDefinitionMeta
                                .put(
                                        var.getSsaVariableInfo().getIR(),
                                        new BlockIdAndInstructionIndex(blockId, instructionIndex)
                                )
                        );
            } else if (instruction instanceof InitInstruction initInstruction) {
                initInstruction.getLhs().getVars()
                        .forEach(var -> nameToDefinitionMeta
                                .put(
                                        var.getSsaVariableInfo().getIR(),
                                        new BlockIdAndInstructionIndex(blockId, instructionIndex)
                                )
                        );
            }
            if (instruction instanceof ForHeaderInstruction forHeaderInstruction) {
                forHeaderInstruction.getDeclarations()
                        .ifPresent(x -> traverseInstruction(x, nameToDefinitionMeta, blockId, instructionIndex));
            }
        }
    }

    private static record BlockIdAndInstructionIndex(int blockId, int instructionIndex) {}
}
