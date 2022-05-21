package kalina.compiler.cfg.builder.items;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.cfg.builder.InstructionCFGBuilder;
import kalina.compiler.cfg.builder.MethodEntryCFGTraverser;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.traverse.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.br.ForEntryEndInstruction;
import kalina.compiler.instructions.v2.br.ForHeaderInstruction;
import org.objectweb.asm.Label;

/**
 * @author vlad333rrty
 */
public class ASTForProcessor extends AbstractBranchExpressionProcessor<ASTForInstruction> {
    private final InstructionCFGBuilder instructionBuilder;

    public ASTForProcessor(
            AbstractExpressionConverter expressionConverter,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider,
            InstructionCFGBuilder instructionCFGBuilder)
    {
        super(expressionConverter, functionInfoProvider, fieldInfoProvider);
        this.instructionBuilder = instructionCFGBuilder;
    }

    public ThenAndElseNodes process(
            ASTForInstruction forInstruction,
            Iterator<ASTExpression> iterator,
            AbstractLocalVariableTable localVariableTable,
            Consumer<Instruction> bbEntryConsumer,
            Consumer<List<Instruction>> blockEndInstructionProvider,
            MethodEntryCFGTraverser traverser) throws CFGConversionException, IncompatibleTypesException
    {
        Label start = new Label();
        ForHeaderAnLabel forHeaderAnLabel = createForHeaderInstruction(forInstruction, localVariableTable, start);
        bbEntryConsumer.accept(forHeaderAnLabel.forHeaderInstruction);
        var endInstruction = new ForEntryEndInstruction(start, forHeaderAnLabel.label);
        AbstractCFGNode thenNode = traverser.traverseScope(forInstruction.entry(), localVariableTable,
                bbs -> bbs.add(endInstruction));
        AbstractCFGNode elseNode = traverser.traverse(iterator, localVariableTable, blockEndInstructionProvider);

        return new ThenAndElseNodes(thenNode, elseNode);
    }

    private ForHeaderAnLabel createForHeaderInstruction(
            ASTForInstruction forInstruction,
            AbstractLocalVariableTable localVariableTable,
            Label start) throws CFGConversionException, IncompatibleTypesException
    {
        AbstractLocalVariableTable childTable = localVariableTable.createChildTable();
        Optional<Instruction> declarations = forInstruction.declarations().isPresent()
                ? Optional.of(instructionBuilder.constructInstruction(forInstruction.declarations().get(), childTable))
                : Optional.empty();
        Optional<CondExpression> condition = forInstruction.condition().isPresent()
                ? Optional.of(expressionConverter.convertCondExpression(forInstruction.condition().get(), childTable, functionInfoProvider, fieldInfoProvider))
                : Optional.empty();
        return new ForHeaderAnLabel(new ForHeaderInstruction(
                declarations,
                condition,
                start
        ), condition.get().getLabel());
    }

    private static record ForHeaderAnLabel(ForHeaderInstruction forHeaderInstruction, Label label) {}
}
