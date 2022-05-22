package kalina.compiler.cfg.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

import kalina.compiler.ast.ASTMethodEntryNode;
import kalina.compiler.ast.expression.ASTBranchExpression;
import kalina.compiler.ast.expression.ASTDoInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.cfg.bb.BasicBlockFactory;
import kalina.compiler.cfg.builder.items.BranchExpressionConverter;
import kalina.compiler.cfg.builder.items.ThenAndElseNodes;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.builder.nodes.CFGNode;
import kalina.compiler.cfg.builder.nodes.CFGNodeWithBranch;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public class MethodEntryCFGTraverser {
    private final InstructionCFGBuilder instructionBuilder;
    private final BranchExpressionConverter branchExpressionConverter;

    public MethodEntryCFGTraverser(InstructionCFGBuilder instructionBuilder, BranchExpressionConverter branchExpressionConverter) {
        this.instructionBuilder = instructionBuilder;
        this.branchExpressionConverter = branchExpressionConverter;
    }

    public AbstractCFGNode traverse(Iterator<ASTExpression> iterator, AbstractLocalVariableTable localVariableTable)
            throws CFGConversionException, IncompatibleTypesException
    {
        return traverse(iterator, localVariableTable, x -> {});
    }

    public AbstractCFGNode traverse(
            Iterator<ASTExpression> iterator,
            AbstractLocalVariableTable localVariableTable,
            Consumer<List<Instruction>> blockEndInstructionProvider) throws CFGConversionException, IncompatibleTypesException
    {
        List<Instruction> bbEntry = new ArrayList<>();
        while (iterator.hasNext()) {
            ASTExpression expression = iterator.next();
            if (expression instanceof ASTBranchExpression branchExpression) {
                ThenAndElseNodes thenAndElseNodes = branchExpressionConverter.convertBranchExpression(
                        branchExpression,
                        iterator,
                        localVariableTable,
                        bbEntry::add,
                        blockEndInstructionProvider,
                        this
                );

                BasicBlock bb = BasicBlockFactory.createBasicBlock(bbEntry);
                CFGNodeWithBranch node = new CFGNodeWithBranch(bb, thenAndElseNodes.thenNode(), thenAndElseNodes.elseNode());
                linkNodes(thenAndElseNodes, branchExpression, node, () -> {
                    try {
                        return traverse(iterator, localVariableTable, blockEndInstructionProvider);
                    } catch (CFGConversionException | IncompatibleTypesException e) {
                        throw new RuntimeException(e);
                    }
                });
                return node;
            } else {
                bbEntry.add(instructionBuilder.constructInstruction(expression, localVariableTable));
            }
        }
        blockEndInstructionProvider.accept(bbEntry);
        return new CFGNode(BasicBlockFactory.createBasicBlock(bbEntry));
    }

    public AbstractCFGNode traverseScope(
            ASTMethodEntryNode node,
            AbstractLocalVariableTable localVariableTable,
            Consumer<List<Instruction>> blockEndInstructionProvider) throws CFGConversionException, IncompatibleTypesException
    {
        AbstractLocalVariableTable childTable = localVariableTable.createChildTable();
        return traverse(node.getExpressions().iterator(), childTable, blockEndInstructionProvider);
    }

    private void linkNodes(
            ThenAndElseNodes thenAndElseNodes,
            ASTBranchExpression branchExpression,
            CFGNodeWithBranch node,
            Supplier<AbstractCFGNode> nextNodeAccessor)
    {
        AbstractCFGNode thenLastNode = DFSImpl.findLastNode(thenAndElseNodes.thenNode());
        if (branchExpression instanceof ASTIfInstruction ifInstruction) {
            if (ifInstruction.elseBr().isPresent()) {
                AbstractCFGNode elseLastNode = DFSImpl.findLastNode(thenAndElseNodes.elseNode());
                AbstractCFGNode nextNode = nextNodeAccessor.get();
                thenLastNode.addChild(nextNode);
                elseLastNode.addChild(nextNode);
                node.setAfterThenElseNode(nextNode);
            } else {
                thenLastNode.addChild(thenAndElseNodes.elseNode());
            }
        }
        if (branchExpression instanceof ASTForInstruction) {
            thenLastNode.addChild(thenAndElseNodes.elseNode());
            thenLastNode.setBackEdgeNode(node);
        }
        if (branchExpression instanceof ASTDoInstruction) {
            thenLastNode.setBackEdgeNode(node);
            thenLastNode.addChild(thenAndElseNodes.elseNode());
        }
    }

    private static class DFSImpl {
        private static Set<Integer> traversedNodes;

        public static AbstractCFGNode findLastNode(AbstractCFGNode node) {
            traversedNodes = new HashSet<>();
            Stack<NodeWithDepth> stack = new Stack<>();
            int maxDepth = 0;
            stack.add(new NodeWithDepth(node, 0));
            traversedNodes.add(node.getId());
            AbstractCFGNode current = node;
            while (!stack.empty()) {
                NodeWithDepth nodeWithDepth = stack.pop();
                int depth = nodeWithDepth.depth;
                AbstractCFGNode cfgNode = nodeWithDepth.node;
                if (cfgNode.getChildren().isEmpty()) {
                    if (depth > maxDepth) {
                        maxDepth = depth;
                        current = cfgNode;
                    }
                } else {
                    stack.addAll(cfgNode.getChildren().stream()
                            .filter(child -> !traversedNodes.contains(child.getId()))
                            .map(n -> new NodeWithDepth(n, depth + 1))
                            .toList());
                }
            }

            return current;
        }

        private static record NodeWithDepth(AbstractCFGNode node, int depth) {}
    }
}
