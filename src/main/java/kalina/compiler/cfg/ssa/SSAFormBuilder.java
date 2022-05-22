package kalina.compiler.cfg.ssa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import kalina.compiler.cfg.bb.PhiFunction;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.SSAVariableInfo;
import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.cfg.dominantTree.DominantTree;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.VariableNameAndIndex;
import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.instructions.v2.br.IfCondInstruction;

/**
 * @author vlad333rrty
 */
public class SSAFormBuilder {
    public void buildSSA(AbstractCFGNode root) {
        List<AbstractCFGNode> nodes = DFSImpl.gatherNodes(root);
        DominantTree dominantTree = new DominantTree(nodes, root);
        PhiFunPlacer phiFunPlacer = new PhiFunPlacer(nodes, dominantTree.getDominanceFrontierProvider());
        Set<String> variableInfos = new HashSet<>();
        nodes.forEach(node -> node.getBasicBlock().getInstructions()
                .forEach(instruction -> {
                    if (instruction instanceof InitInstruction initInstruction) {
                        variableInfos.addAll(initInstruction.getLhs().getVars().stream().map(VariableNameAndIndex::getName).toList());
                    } else if (instruction instanceof AbstractAssignInstruction assignInstruction) {
                        variableInfos.addAll(assignInstruction.getLhs().stream().map(VariableInfo::getName).toList());
                    }
                }));
        for (var varInfo : variableInfos) {
            phiFunPlacer.placeForVar(varInfo);
        }
        RenameVariablePerformer renameVariablePerformer = new RenameVariablePerformer(nodes);
        for (var name : variableInfos) {
            renameVariablePerformer.renameForVariable(name, root);
        }
    }

    private static class RenameVariablePerformer {
        private final Map<String, Stack<Integer>> varInfoToVersionStack = new HashMap<>();
        private final Map<String, Integer> varInfoToCounter = new HashMap<>();

        private final List<AbstractCFGNode> nodes;

        public RenameVariablePerformer(List<AbstractCFGNode> nodes) {
            this.nodes = nodes;
            fillVarInfoToCounter();
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
            for (Instruction instruction : node.getBasicBlock().getInstructions()) {
                if (instruction instanceof AbstractAssignInstruction assignInstruction) {
                    List<Expression> rhs = assignInstruction.getRhs().stream()
                            .map(x -> substituteExpression(x, varName))
                            .toList();
                    instructions.add(assignInstruction.withRHS(rhs));
                    assignInstruction.getLhs().stream()
                            .filter(x -> x.getName().equals(varName))
                            .forEach(lhs -> updateVersion(lhs.getSsaVariableInfo()));
                } else if (instruction instanceof IfCondInstruction ifCondInstruction) {
                    CondExpression condExpression = (CondExpression)substituteExpression(ifCondInstruction.getCondition(), varName);
                    instructions.add(new IfCondInstruction(condExpression));
                } else if (instruction instanceof InitInstruction initInstruction) {
                    List<Expression> expressions = initInstruction.getRhs().stream()
                            .map(x -> substituteExpression(x, varName))
                            .toList();
                    instructions.add(initInstruction.withRHS(expressions));
                    initInstruction.getLhs().getVars().stream()
                            .filter(x -> x.getName().equals(varName))
                            .forEach(var -> updateVersion(var.getSsaVariableInfo()));
                }
                else {
                    instructions.add(instruction);
                }
            }
            node.getBasicBlock().setInstructions(instructions);
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
                        .map(x -> (Term)x)
                        .toList();
                return arithmeticExpression.withTerms(terms);
            } else if (expression instanceof Term term) {
                List<Factor> factors = term.getFactors().stream()
                        .map(x -> substituteExpression(x, varName))
                        .map(x -> (Factor)x)
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
            }

            return expression;
        }

        private boolean containsVarAssign(AbstractAssignInstruction assignInstruction, String varName) {
            return assignInstruction.getLhs().stream().anyMatch(x -> x.getName().equals(varName));
        }

        private boolean containsVarAssign(InitInstruction instruction, String varName) {
            return instruction.getLhs().getVars().stream().anyMatch(var -> var.getName().equals(varName));
        }

        private void updateVersion(SSAVariableInfo varInfo) {
            int i = varInfoToCounter.get(varInfo.getName());
            varInfo.setCfgIndex(i);
            varInfoToVersionStack.get(varInfo.getName()).push(i);
            varInfoToCounter.put(varInfo.getName(), i + 1);
        }

        private void fillVarInfoToCounter() {
            nodes.forEach(node -> node.getBasicBlock().getInstructions().forEach(instruction -> {
                if (instruction instanceof AbstractAssignInstruction assignInstruction) {
                    assignInstruction.getLhs().forEach(var -> varInfoToCounter.put(var.getSsaVariableInfo().getName(), 0));
                }
                if (instruction instanceof InitInstruction initInstruction) {
                    initInstruction.getLhs().getVars().forEach(var -> varInfoToCounter.put(var.getName(), 0));
                }
            }));
        }
    }

    private static class PhiFunPlacer {
        private final List<AbstractCFGNode> nodes;
        private final Function<Integer, Set<AbstractCFGNode>> dominanceFrontierProvider;
        private final Set<String> processedVars;

        public PhiFunPlacer(List<AbstractCFGNode> nodes, Function<Integer, Set<AbstractCFGNode>> dominanceFrontierProvider) {
            this.nodes = nodes;
            this.dominanceFrontierProvider = dominanceFrontierProvider;
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
                            .anyMatch(instruction -> instruction instanceof AbstractAssignInstruction assignInstruction
                                    && assignInstruction.getLhs().stream()
                                    .map(VariableInfo::getName).anyMatch(x -> x.equals(varName))))
                    .forEach(node -> {
                        processedNodes.add(node.getId());
                        queue.add(node);
                    });
            while (!queue.isEmpty()) {
                AbstractCFGNode v = queue.poll();
                Set<AbstractCFGNode> dominanceFrontier = dominanceFrontierProvider.apply(v.getId());
                for (var frontierNode : dominanceFrontier) {
                    Optional<PhiFunction> phiFunO = frontierNode.getBasicBlock().getPhiFunForVar(varName);
                    if (phiFunO.isEmpty()) {
                        frontierNode.getBasicBlock().addPhiFunForVar(varName);
                        if (processedNodes.add(frontierNode.getId())) {
                            queue.add(frontierNode);
                        }
                    }
                }
            }
            processedVars.add(varName);
        }
    }

    private static class DFSImpl {
        public static List<AbstractCFGNode> gatherNodes(AbstractCFGNode root) {
            Set<Integer> traversedNodes = new HashSet<>();
            Stack<AbstractCFGNode> stack = new Stack<>();
            stack.push(root);
            List<AbstractCFGNode> nodes = new ArrayList<>();
            while (!stack.isEmpty()) {
                AbstractCFGNode v = stack.pop();
                nodes.add(v);
                traversedNodes.add(v.getId());
                v.getChildren().stream()
                        .filter(child -> !traversedNodes.contains(child.getId()))
                        .forEach(stack::push);
            }

            return nodes;
        }
    }
}
