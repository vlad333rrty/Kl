package kalina.compiler.cfg.optimizations.cp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.bb.BasicBlock;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.VariableInfo;
import kalina.compiler.cfg.data.WithIR;
import kalina.compiler.cfg.optimizations.ConstantExpressionDetector;
import kalina.compiler.cfg.optimizations.DuUdNet;
import kalina.compiler.cfg.optimizations.DuUdNetBuilder;
import kalina.compiler.cfg.optimizations.ExpressionSubstitutor;
import kalina.compiler.cfg.optimizations.OptimizationsUtils;
import kalina.compiler.cfg.optimizations.cf.CfArithmeticExpressionParser;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.VariableNameAndIndex;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.instructions.v2.WithCondition;
import kalina.compiler.instructions.v2.WithExpressions;
import kalina.compiler.instructions.v2.assign.ArrayElementAssign;
import kalina.compiler.instructions.v2.assign.AssignInstruction;
import kalina.compiler.instructions.v2.fake.FakeAssignInstruction;
import kalina.compiler.instructions.v2.fake.FakeValueExpression;
import kalina.compiler.instructions.v2.fake.PhiArgument;
import kalina.compiler.instructions.v2.fake.PhiFunInstruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 *
 * needs severe fixing
 */
public class ConstantPropagationPerformer {
    private static final Logger logger = LogManager.getLogger(ConstantPropagationPerformer.class);

    public void perform(ControlFlowGraph controlFlowGraph) {
        Map<Integer, BasicBlock> idToBB = controlFlowGraph.nodes().stream()
                .collect(Collectors.toMap(
                        AbstractCFGNode::getId,
                        AbstractCFGNode::getBasicBlock
                ));
        DuUdNet duUdNet = DuUdNetBuilder.buildDuUdNet(controlFlowGraph.root());
        Queue<DuUdNet.InstructionCoordinates> workList = new ArrayDeque<>(
                controlFlowGraph.root().getChildren().stream()
                        .flatMap(node -> {
                            int blockId = node.getId();
                            List<DuUdNet.InstructionCoordinates> result = new ArrayList<>();
                            for (int i = 0; i < node.getBasicBlock().getInstructions().size(); i++) {
                                result.add(new DuUdNet.InstructionCoordinates(blockId, i));
                            }
                            return result.stream();
                        })
                        .toList());
        Map<String, ValueExpression> nameToConstantValue = new HashMap<>();
        while (!workList.isEmpty()) {
            DuUdNet.InstructionCoordinates coordinates = workList.poll();
            BasicBlock bb = idToBB.get(coordinates.blockId());
            Instruction instruction = OptimizationsUtils.getBBInstruction(bb, coordinates);
            if (instruction instanceof AssignInstruction assignInstruction) {
                int bound = assignInstruction.getLhs().size();
                for (int i = 0; i < bound; i++) {
                    VariableInfo variableInfo = assignInstruction.getLhs().get(i);
                    Expression rhs = assignInstruction.getRHS().get(i);
                    var expr =
                            visitInitOrAssign(variableInfo, rhs, nameToConstantValue, idToBB, coordinates, duUdNet, workList);
                    if (expr != null) {
                        List<Expression> newRhs = new ArrayList<>(assignInstruction.getRHS());
                        newRhs.set(i, expr);
                        setInstruction(bb, coordinates, ((AssignInstruction) instruction).withRHS(newRhs));
                    }
                }
            }
            if (instruction instanceof InitInstruction initInstruction) {
                for (int i = 0; i < initInstruction.getLhs().size(); i++) {
                    VariableNameAndIndex variableInfo = initInstruction.getLhs().getVars().get(i);
                    Expression rhs = initInstruction.getRHS().get(i);
                    var expr =
                            visitInitOrAssign(variableInfo, rhs, nameToConstantValue, idToBB, coordinates, duUdNet, workList);
                    if (expr != null) {
                        List<Expression> newRhs = new ArrayList<>(initInstruction.getRHS());
                        newRhs.set(i, expr);
                        setInstruction(bb, coordinates, ((InitInstruction) instruction).substituteExpressions(newRhs));
                    }
                }
            }
            if (instruction instanceof PhiFunInstruction phiFunInstruction) {
                var inst = visitPhi(phiFunInstruction, nameToConstantValue);
                if (inst instanceof FakeAssignInstruction fakeAssignInstruction) {
                    visitInitOrAssign(
                            fakeAssignInstruction,
                            new ValueExpression(fakeAssignInstruction.getValue(), getOriginType(nameToConstantValue, fakeAssignInstruction.getIR())),
                            nameToConstantValue,
                            idToBB,
                            coordinates,
                            duUdNet,
                            workList);
                }
            }
        }
    }

    private Type getOriginType(Map<String, ValueExpression> nameToConstantValue, String name) {
        return nameToConstantValue.get(name.substring(0, name.lastIndexOf("_")) + "_0").getType();
    }

    private boolean isConstant(Expression expression) {
        return ConstantExpressionDetector.isNumberConstant(expression);
    }

    private Expression visitInitOrAssign(
            WithIR lhs,
            Expression rhs,
            Map<String, ValueExpression> nameToConstantValue,
            Map<Integer, BasicBlock> idToBB,
            DuUdNet.InstructionCoordinates coordinates,
            DuUdNet duUdNet,
            Queue<DuUdNet.InstructionCoordinates> workList)
    {
        String ir = lhs.getIR();
        Expression expr = null;
        if (isConstant(rhs)) {
            if (rhs instanceof ValueExpression valueExpression) {
                nameToConstantValue.computeIfAbsent(ir, k -> new ValueExpression(valueExpression.getValue(), valueExpression.getType()));
            } else {
                Expression expression = CfArithmeticExpressionParser.parseExpression(rhs);
                if (expression instanceof ValueExpression valueExpression) {
                    expr = expression;
                    nameToConstantValue.computeIfAbsent(ir, k -> new ValueExpression(valueExpression.getValue(), valueExpression.getType()));
                } else {
                    return null;
                }
            }
            DuUdNet.Definition definition =
                    new DuUdNet.Definition(ir, coordinates.blockId(), coordinates.instructionIndex());
            List<DuUdNet.InstructionCoordinates> uses = duUdNet.getDuChainProvider().apply(definition);
            logger.info("{} -- {}", definition, uses);
            for (var use : uses) {
                BasicBlock useBB = idToBB.get(use.blockId());
                Instruction useInstruction = OptimizationsUtils.getBBInstruction(useBB, use);
                Instruction transformedInstruction = visitInstruction(useInstruction, ir, nameToConstantValue);
                setInstruction(useBB, use, transformedInstruction);
                workList.add(use);
            }
        }
        return expr;
    }

    private void setInstruction(BasicBlock basicBlock, DuUdNet.InstructionCoordinates coordinates, Instruction instruction) {
        if (coordinates.instructionIndex() < basicBlock.getPhiFunInstructions().size()) {
            // nothing
        } else {
            int offset = basicBlock.getPhiFunInstructions().size();
            basicBlock.getInstructions().set(coordinates.instructionIndex() - offset, instruction);
        }
    }

    private Instruction visitInstruction(Instruction instruction, String varName, Map<String, ValueExpression> nameToConstantValue) {
        if (instruction instanceof WithExpressions withExpressions) {
            List<Expression> expressions = withExpressions.getExpressions().stream()
                    .map(expr -> substituteExpression(expr, varName, nameToConstantValue))
                    .map(this::reduceIfPossible)
                    .toList();
            return withExpressions.substituteExpressions(expressions);
        }
        if (instruction instanceof WithCondition withCondition) {
            CondExpression condExpression = (CondExpression) substituteExpression(withCondition.getCondExpression(), varName, nameToConstantValue);
            return withCondition.substituteCondExpression(condExpression);
        }
        if (instruction instanceof PhiFunInstruction phiFunInstruction) {
            for (int i = 1; i < phiFunInstruction.filterAndGetArguments().size(); i++) {
                var phiArg = phiFunInstruction.filterAndGetArguments().get(i);
                if (phiArg.getSsaVariableInfo().getIR().equals(varName)) {
                    List<PhiArgument> arguments = new ArrayList<>(phiFunInstruction.getAllArguments());
                    arguments.set(i, new FakeValueExpression(nameToConstantValue.get(varName)));
                    phiFunInstruction.setArguments(arguments);
                }
            }
        }
        if (instruction instanceof ArrayElementAssign arrayElementAssign) {
            arrayElementAssign.getAssignArrayVariableInfo()
                    .forEach(arrayVariableInfo -> {
                        List<Expression> substitutedIndices = arrayVariableInfo.getIndices().stream()
                                .map(index -> substituteExpression(index, varName, nameToConstantValue))
                                .map(this::reduceIfPossible)
                                .toList();
                        arrayVariableInfo.setIndices(substitutedIndices);
                    });
        }
        return instruction;
    }

    private Expression reduceIfPossible(Expression expression) {
        return isConstant(expression) ? CfArithmeticExpressionParser.parseExpression(expression) : expression;
    }

    private Expression substituteExpression(
            Expression expression,
            String varName,
            Map<String, ValueExpression> nameToConstantValue) {
        return ExpressionSubstitutor.substituteExpression(
                expression,
                varName,
                ve -> {
                    if (!ve.getSsaVariableInfo().getIR().equals(varName)) {
                        return ve;
                    }
                    ValueExpression value = nameToConstantValue.get(varName);
                    if (value != null) {
                        return value;
                    }
                    logger.error("No value expression found for constant variable {}", varName);
                    return ve;
                },
                Function.identity()
        );
    }

    private Instruction visitPhi(
            PhiFunInstruction phiFunInstruction,
            Map<String, ValueExpression> nameToValue) {
        if (phiFunInstruction.filterAndGetArguments().stream()
                .allMatch(x -> nameToValue.containsKey(x.getSsaVariableInfo().getIR()))) {
            ValueExpression firstVal = nameToValue.get(phiFunInstruction.filterAndGetArguments().get(0).getSsaVariableInfo().getIR());
            for (int i = 1; i < phiFunInstruction.filterAndGetArguments().size(); i++) {
                ValueExpression value = nameToValue.get(phiFunInstruction.filterAndGetArguments().get(i).getSsaVariableInfo().getIR());
                if (!value.getValue().equals(firstVal.getValue())) {
                    return phiFunInstruction;
                }
            }

            return new FakeAssignInstruction(phiFunInstruction.getLhsIR().getIR(), firstVal.getValue());
        }

        return phiFunInstruction;
    }
}
