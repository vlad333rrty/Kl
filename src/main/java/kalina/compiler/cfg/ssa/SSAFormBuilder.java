package kalina.compiler.cfg.ssa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.cfg.bb.PhiFunction;
import kalina.compiler.cfg.bb.PhiFunctionHolder;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.cfg.dominantTree.DominantTree;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.array.ArrayGetElementExpression;
import kalina.compiler.expressions.v2.array.ArrayWithCapacityCreationExpression;
import kalina.compiler.expressions.v2.array.FieldArrayGetElementExpression;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.WithCondition;
import kalina.compiler.instructions.v2.WithExpressions;
import kalina.compiler.instructions.v2.WithLHS;
import kalina.compiler.instructions.v2.assign.ArrayElementAssign;
import kalina.compiler.instructions.v2.assign.ArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.fake.PhiArgumentExpression;
import kalina.compiler.instructions.v2.fake.PhiFunInstruction;

/**
 * @author vlad333rrty
 */
public class SSAFormBuilder {
    public void buildSSA(ControlFlowGraph controlFlowGraph) {
        AbstractCFGNode root = controlFlowGraph.root();
        List<AbstractCFGNode> nodes = controlFlowGraph.nodes();
        DominantTree dominantTree = new DominantTree(nodes, root);

        Map<Integer, PhiFunctionHolder> blockIdToPhiFunHolder = controlFlowGraph.nodes().stream()
                .collect(Collectors.toMap(
                        AbstractCFGNode::getId,
                        x -> new PhiFunctionHolder()
                ));

        PhiFunPlacer phiFunPlacer = new PhiFunPlacer(nodes, dominantTree.getDominanceFrontierProvider(), blockIdToPhiFunHolder);
        Set<String> variableInfos = nodes.stream()
                .flatMap(node -> node.getBasicBlock().getInstructions()
                        .stream()
                        .map(this::getVariableNames)
                        .flatMap(Collection::stream)
                )
                .collect(Collectors.toSet());
        for (var varInfo : variableInfos) {
            phiFunPlacer.placeForVar(varInfo);
        }
        RenameVariablePerformer renameVariablePerformer = new RenameVariablePerformer(variableInfos, blockIdToPhiFunHolder);
        for (var name : variableInfos) {
            renameVariablePerformer.renameForVariable(name, root);
        }
        for (var node : controlFlowGraph.nodes()) {
            putPhiInstructionsIntoBasicBlock(node.getBasicBlock(), blockIdToPhiFunHolder.get(node.getId()));
        }
    }

    private void putPhiInstructionsIntoBasicBlock(BasicBlock basicBlock, PhiFunctionHolder phiFunctionHolder) {
        List<PhiFunInstruction> phiFunInstructions = new ArrayList<>();
        for (var entry : phiFunctionHolder.getVarInfoToPhiFun().entrySet()) {
            List<PhiArgumentExpression> phiArgs = entry.getValue().getArguments().stream()
                    .map(x -> new PhiArgumentExpression(x.getName(), x.getCfgIndex()))
                    .toList();
            phiFunInstructions.add(new PhiFunInstruction(phiArgs, entry.getKey().getIR()));
        }
        basicBlock.setPhiFunInstructions(Collections.unmodifiableList(phiFunInstructions));
    }

    private Set<String> getVariableNames(Instruction instruction) {
        Set<String> variableNames = new HashSet<>();
        if (instruction instanceof WithLHS withLHS) {
            variableNames.addAll(withLHS.getVariableInfos().stream().map(SSAVariableInfo::getName).toList());
        }
        return variableNames;
    }

    private static class RenameVariablePerformer {
        private final Map<String, Stack<Integer>> varInfoToVersionStack = new HashMap<>();
        private final Map<String, Integer> varInfoToCounter = new HashMap<>();

        private final Map<Integer, PhiFunctionHolder> blockIdToPhiFunHolder;

        public RenameVariablePerformer(Set<String> varNames, Map<Integer, PhiFunctionHolder> blockIdToPhiFunHolder) {
            this.blockIdToPhiFunHolder = blockIdToPhiFunHolder;
            varNames.forEach(name -> varInfoToCounter.put(name, 0));
            varInfoToCounter.forEach((key, value) -> varInfoToVersionStack.put(key, initStack()));
        }

        private Stack<Integer> initStack() {
            Stack<Integer> stack = new Stack<>();
            stack.push(0);
            return stack;
        }

        public void renameForVariable(String varName, AbstractCFGNode node) {
            renameForVariableInt(varName, node, new HashSet<>());
        }

        public void renameForVariableInt(String varName, AbstractCFGNode node, Set<Integer> traversedNodes) {
            traversedNodes.add(node.getId());
            Stack<Integer> stack = varInfoToVersionStack.get(varName);
            int version = stack.peek();
            List<Instruction> instructions = new ArrayList<>();

            Set<Map.Entry<SSAVariableInfo, PhiFunction>> entrySet =
                    blockIdToPhiFunHolder.get(node.getId()).getVarInfoToPhiFun().entrySet();
            for (var entry : entrySet) {
                if (entry.getKey().getName().equals(varName)) {
                    updateVersion(entry.getKey());
                }
            }
            for (Instruction instruction : node.getBasicBlock().getInstructions()) {
                if (instruction instanceof WithExpressions withExpressions) {
                    List<Expression> expressions = withExpressions.getExpressions().stream()
                            .map(expr -> substituteExpression(expr, varName))
                            .toList();
                    instructions.add(withExpressions.substituteExpressions(expressions));
                } else if (instruction instanceof WithCondition withCondition) {
                    CondExpression condExpression = (CondExpression) substituteExpression(withCondition.getCondExpression(), varName);
                    instructions.add(withCondition.substituteCondExpression(condExpression));
                } else {
                    instructions.add(instruction);
                }
                if (instruction instanceof WithLHS withLHS) {
                    withLHS.getVariableInfos().stream()
                            .filter(info -> info.getName().equals(varName))
                            .forEach(this::updateVersion);
                }
                if (instruction instanceof ArrayElementAssignInstruction assignInstruction) {
                    assignInstruction.getLhs().stream()
                            .filter(info -> info.getName().equals(varName))
                            .forEach(info -> {
                                int i = varInfoToVersionStack.get(info.getName()).peek();
                                info.getSsaVariableInfo().setCfgIndex(i);
                            });
                }
                if (instruction instanceof ArrayElementAssign arrayElementAssign) {
                    arrayElementAssign.getAssignArrayVariableInfo()
                            .forEach(arrayVariableInfo -> {
                                List<Expression> substitutedIndices = arrayVariableInfo.getIndices().stream()
                                        .map(index -> substituteExpression(index, varName))
                                        .toList();
                                arrayVariableInfo.setIndices(substitutedIndices);
                            });
                }
            }
            node.getBasicBlock().setInstructions(instructions);
            int nextVersion = stack.peek();
            for (var child : node.getChildren()) {
                PhiFunctionHolder phiFunctionHolder = blockIdToPhiFunHolder.get(child.getId());
                if (phiFunctionHolder.getForVar(varName).isPresent()) {
                    phiFunctionHolder.updatePhiFunArgument(varName, nextVersion, node.getId());
                }
            }
            for (var child : node.getChildren()) {
                if (!traversedNodes.contains(child.getId())) {
                    renameForVariableInt(varName, child, traversedNodes);
                }
            }
            while (stack.peek() != version) {
                stack.pop();
            }
        }

        private Expression substituteExpression(Expression expression, String varName) {
            if (expression instanceof VariableExpression variableExpression) {
                String name = variableExpression.getName();
                if (!name.equals(varName)) {
                    return expression;
                }
                int version = varInfoToVersionStack.get(name).peek();
                return variableExpression.withCfgIndex(version);
            } else if (expression instanceof ArithmeticExpression arithmeticExpression) {
                List<Term> terms = arithmeticExpression.getTerms().stream()
                        .map(x -> substituteExpression(x, varName))
                        .map(x -> (Term) x)
                        .toList();
                return arithmeticExpression.withTerms(terms);
            } else if (expression instanceof Term term) {
                List<Factor> factors = term.getFactors().stream()
                        .map(x -> substituteExpression(x, varName))
                        .map(x -> (Factor) x)
                        .toList();
                return term.withFactors(factors);
            } else if (expression instanceof Factor factor) {
                return factor.withExpression(substituteExpression(factor.getExpression(), varName));
            } else if (expression instanceof CondExpression condExpression) {
                List<Expression> expressions = condExpression.getExpressions().stream()
                        .map(x -> substituteExpression(x, varName))
                        .toList();
                return condExpression.substituteExpressions(expressions);
            } else if (expression instanceof AbstractFunCallExpression funCallExpression) {
                List<Expression> arguments = funCallExpression.getArguments().stream()
                        .map(x -> substituteExpression(x, varName))
                        .toList();
                return funCallExpression.substituteArguments(arguments);
            } else if (expression instanceof ArrayGetElementExpression arrayGetElementExpression) {
                List<Expression> indices = arrayGetElementExpression.getIndices().stream()
                        .map(x -> substituteExpression(x, varName))
                        .toList();
                String name = arrayGetElementExpression.getName();
                if (name.equals(varName)) {
                    int version = varInfoToVersionStack.get(name).peek();
                    return arrayGetElementExpression.withCfgIndex(version).substituteExpressions(indices);
                }
                return arrayGetElementExpression.substituteExpressions(indices);
            } else if (expression instanceof ArrayWithCapacityCreationExpression arrayWithCapacityCreationExpression) {
                List<Expression> capacities = arrayWithCapacityCreationExpression.getCapacities().stream()
                        .map(x -> substituteExpression(x, varName))
                        .toList();
                return arrayWithCapacityCreationExpression.substituteExpressions(capacities);
            } else if (expression instanceof FieldArrayGetElementExpression fieldArrayGetElementExpression) {
                List<Expression> indices = fieldArrayGetElementExpression.getIndices().stream()
                        .map(x -> substituteExpression(x, varName))
                        .toList();
                return fieldArrayGetElementExpression.substituteExpressions(indices);
            }

            return expression;
        }

        private void updateVersion(SSAVariableInfo varInfo) {
            int i = varInfoToCounter.get(varInfo.getName());
            varInfo.setCfgIndex(i);
            varInfoToVersionStack.get(varInfo.getName()).push(i);
            varInfoToCounter.put(varInfo.getName(), i + 1);
        }
    }

    private static class PhiFunPlacer {
        private final List<AbstractCFGNode> nodes;
        private final Function<Integer, Set<AbstractCFGNode>> dominanceFrontierProvider;
        private final Map<Integer, PhiFunctionHolder> blockIdToPhiFunHolder;
        private final Set<String> processedVars;

        public PhiFunPlacer(
                List<AbstractCFGNode> nodes,
                Function<Integer, Set<AbstractCFGNode>> dominanceFrontierProvider,
                Map<Integer, PhiFunctionHolder> blockIdToPhiFunHolder)
        {
            this.nodes = nodes;
            this.dominanceFrontierProvider = dominanceFrontierProvider;
            this.blockIdToPhiFunHolder = blockIdToPhiFunHolder;
            this.processedVars = new HashSet<>();
        }

        public void placeForVar(String varName) {
            if (processedVars.contains(varName)) {
                return;
            }
            Set<Integer> processedNodes = new HashSet<>();
            Queue<AbstractCFGNode> queue = new ArrayDeque<>();
            nodes.stream()
                    .filter(node -> node.getBasicBlock().getInstructions().stream()
                            .anyMatch(instruction -> getAssignInstructionsFilter(instruction, varName)))
                    .forEach(node -> {
                        processedNodes.add(node.getId());
                        queue.add(node);
                    });
            while (!queue.isEmpty()) {
                AbstractCFGNode v = queue.poll();
                Set<AbstractCFGNode> dominanceFrontier = dominanceFrontierProvider.apply(v.getId());
                for (var frontierNode : dominanceFrontier) {
                    PhiFunctionHolder phiFunctionHolder = blockIdToPhiFunHolder.get(frontierNode.getId());
                    Optional<PhiFunction> phiFunO = phiFunctionHolder.getForVar(varName);
                    if (phiFunO.isEmpty()) {
                        int dim = calcPhiFunDimensionAndSetArgCorrespondence(frontierNode);
                        if (dim > 1) { // no need to make phi fun if there is no "chose" between variables in cfg
                            phiFunctionHolder.addPhiFun(varName, dim);
                        }
                        if (processedNodes.add(frontierNode.getId())) {
                            queue.add(frontierNode);
                        }
                    }
                }
            }
            processedVars.add(varName);
        }

        private boolean getAssignInstructionsFilter(Instruction instruction, String varName) {
            if (instruction instanceof WithLHS withLHS) {
                return withLHS.getVariableInfos().stream()
                        .map(SSAVariableInfo::getName)
                        .anyMatch(x -> x.equals(varName));
            }
            return false;
        }

        private int calcPhiFunDimensionAndSetArgCorrespondence(AbstractCFGNode node) {
            int dim = 0;
            PhiFunctionHolder phiFunHolder = blockIdToPhiFunHolder.get(node.getId());
            for (var ancestor : node.getAncestors()) {
                phiFunHolder.putBlockIdToPhiFunArg(ancestor.getId(), dim);
                dim++;
            }

            return dim;
        }
    }
}
