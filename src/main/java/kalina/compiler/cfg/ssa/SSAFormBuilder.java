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

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.bb.PhiFunction;
import kalina.compiler.cfg.bb.PhiFunctionHolder;
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
import kalina.compiler.instructions.v2.WithCondition;
import kalina.compiler.instructions.v2.WithExpressions;
import kalina.compiler.instructions.v2.br.IfCondInstruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class SSAFormBuilder {
    private static final Logger logger = LogManager.getLogger(SSAFormBuilder.class);

    public void buildSSA(ControlFlowGraph controlFlowGraph) {
        AbstractCFGNode root = controlFlowGraph.root();
        List<AbstractCFGNode> nodes = controlFlowGraph.nodes();
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

            for (var entry : node.getBasicBlock().getVarInfoToPhiFun().entrySet()) {
                if (entry.getKey().getName().equals(varName)) {
                    updateVersion(entry.getKey());
                }
            }
            for (Instruction instruction : node.getBasicBlock().getInstructions()) {
                if (instruction instanceof AbstractAssignInstruction assignInstruction) {
                    List<Expression> rhs = assignInstruction.getRhs().stream()
                            .map(x -> substituteExpression(x, varName))
                            .toList();
                    instructions.add(assignInstruction.withRHS(rhs));
                    assignInstruction.getLhs().stream()
                            .filter(x -> x.getName().equals(varName))
                            .forEach(lhs -> updateVersion(lhs.getSsaVariableInfo()));
                } else if (instruction instanceof WithCondition ifCondInstruction) {
                    CondExpression condExpression = (CondExpression)substituteExpression(ifCondInstruction.getCondExpression(), varName);
                    instructions.add(new IfCondInstruction(condExpression)); // todo
                } else if (instruction instanceof InitInstruction initInstruction) {
                    List<Expression> expressions = initInstruction.getRhs().stream()
                            .map(x -> substituteExpression(x, varName))
                            .toList();
                    instructions.add(initInstruction.withRHS(expressions));
                    initInstruction.getLhs().getVars().stream()
                            .filter(x -> x.getName().equals(varName))
                            .forEach(var -> updateVersion(var.getSsaVariableInfo()));
                } else if (instruction instanceof WithExpressions withExpressions) {
                    List<Expression> expressions = withExpressions.getExpressions().stream()
                            .map(expr -> substituteExpression(expr, varName))
                            .toList();
                    instructions.add(withExpressions.substituteExpressions(expressions));
                }
                else {
                    instructions.add(instruction);
                }
            }
            node.getBasicBlock().setInstructions(instructions);
            int nextVersion = stack.peek();
            for (var child : node.getChildren()) {
                if (child.getBasicBlock().getPhiFunForVar(varName).isPresent()) {
                    child.getBasicBlock().updatePhiFunArg(varName, nextVersion, node.getId());
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
                        int dim = calcPhiFunDimension(frontierNode, varName);
                        if (dim > 1) { // no need to make phi fun if there is no "chose" between variables in cfg
                            frontierNode.getBasicBlock().addPhiFunForVar(varName, dim);
                        }
                        if (processedNodes.add(frontierNode.getId())) {
                            queue.add(frontierNode);
                        }
                    }
                }
            }
            processedVars.add(varName);
        }

        private int calcPhiFunDimension(AbstractCFGNode node, String varName) {
            int dim = 0;
            PhiFunctionHolder phiFunHolder = node.getBasicBlock().getPhiFunctionHolder();
            for (var ancestor : node.getAncestors()) {
                int blockId = ancestor.getId();
                boolean flag = false;
                for (Instruction instruction : ancestor.getBasicBlock().getInstructions()) {
                    if (instruction instanceof AbstractAssignInstruction assignInstruction
                            && containsVarAssign(assignInstruction, varName))
                    {
                        phiFunHolder.putBlockIdToPhiFunArg(blockId, dim);
                        dim++;
                        flag = true;
                        break;
                    } else if (instruction instanceof InitInstruction initInstruction
                            && containsVarAssign(initInstruction, varName))
                    {
                        phiFunHolder.putBlockIdToPhiFunArg(blockId, dim);
                        dim++;
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                }
                for (var entry : ancestor.getBasicBlock().getVarInfoToPhiFun().entrySet()) {
                    if (entry.getKey().getName().equals(varName)) {
                        phiFunHolder.putBlockIdToPhiFunArg(blockId, dim);
                        dim++;
                        break;
                    }
                }
            }

            return dim;
        }

        private boolean containsVarAssign(AbstractAssignInstruction assignInstruction, String varName) {
            return assignInstruction.getLhs().stream().anyMatch(x -> x.getName().equals(varName));
        }

        private boolean containsVarAssign(InitInstruction instruction, String varName) {
            return instruction.getLhs().getVars().stream().anyMatch(var -> var.getName().equals(varName));
        }
    }
}
