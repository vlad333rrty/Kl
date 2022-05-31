package kalina.compiler.cfg.builder.items;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.cfg.bb.BasicBlockFactory;
import kalina.compiler.cfg.builder.instruction.AbstractInstructionCFGBuilder;
import kalina.compiler.cfg.builder.MethodEntryCFGTraverser;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.builder.nodes.CFGNodeWithBranch;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.br._for.ForCondInstruction;
import kalina.compiler.instructions.v2.br._for.ForDeclarationInstruction;
import kalina.compiler.instructions.v2.br._for.ForEntryEndInstruction;
import org.objectweb.asm.Label;

/**
 * @author vlad333rrty
 */
public class ASTForProcessor extends AbstractBranchExpressionProcessor<ASTForInstruction> {
    private final AbstractInstructionCFGBuilder instructionBuilder;

    public ASTForProcessor(
            AbstractExpressionConverter expressionConverter,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider,
            AbstractInstructionCFGBuilder instructionCFGBuilder)
    {
        super(expressionConverter, functionInfoProvider, fieldInfoProvider);
        this.instructionBuilder = instructionCFGBuilder;
    }

    @Override
    public ThenAndElseNodes process(
            ASTForInstruction forInstruction,
            Iterator<ASTExpression> iterator,
            AbstractLocalVariableTable localVariableTable,
            Consumer<Instruction> bbEntryConsumer,
            Consumer<List<Instruction>> blockEndInstructionProvider,
            Consumer<List<Instruction>> blockStartInstructionProvider,
            MethodEntryCFGTraverser traverser) throws CFGConversionException, IncompatibleTypesException
    {
        Label start = new Label();
        AbstractLocalVariableTable childTable = localVariableTable.createChildTable();
        ForHeaderAndLabel forHeaderAndLabel = createForHeaderInstruction(forInstruction, childTable, start);
        bbEntryConsumer.accept(forHeaderAndLabel.forDeclarationInstruction);
       // bbEntryConsumer.accept(forHeaderAndLabel.forCondInstruction);
        Optional<Instruction> action = forInstruction.action().isPresent()
                ? Optional.of(instructionBuilder.constructInstruction(forInstruction.action().get(), childTable))
                : Optional.empty();
        var endInstruction = new ForEntryEndInstruction(start, forHeaderAndLabel.label, action);
        AbstractCFGNode thenNode = traverser.traverse(
                forInstruction.entry().getExpressions().iterator(),
                childTable,
                bbs -> bbs.add(endInstruction),
                blockStartInstructionProvider);
        AbstractCFGNode elseNode = traverser.traverse(iterator, localVariableTable, blockEndInstructionProvider, blockStartInstructionProvider);

        CFGNodeWithBranch condNode = new CFGNodeWithBranch(
                BasicBlockFactory.createBasicBlock(List.of(forHeaderAndLabel.forCondInstruction)),
                thenNode,
                elseNode
        );
        return new ThenAndElseNodes(thenNode, elseNode, Optional.of(condNode));
    }

    private ForHeaderAndLabel createForHeaderInstruction(
            ASTForInstruction forInstruction,
            AbstractLocalVariableTable localVariableTable,
            Label start) throws CFGConversionException, IncompatibleTypesException
    {
        Optional<Instruction> declarations = forInstruction.declarations().isPresent()
                ? Optional.of(instructionBuilder.constructInstruction(forInstruction.declarations().get(), localVariableTable))
                : Optional.empty();
        Optional<CondExpression> condition = forInstruction.condition().isPresent()
                ? Optional.of(expressionConverter.convertCondExpression(forInstruction.condition().get(), localVariableTable, functionInfoProvider, fieldInfoProvider))
                : Optional.empty();
        ForDeclarationInstruction forDeclarationInstruction = new ForDeclarationInstruction(declarations);
        ForCondInstruction forCondInstruction = new ForCondInstruction(condition.get(), start);
        return new ForHeaderAndLabel(forDeclarationInstruction, forCondInstruction, condition.get().getLabel());
    }

    private static record ForHeaderAndLabel(
            ForDeclarationInstruction forDeclarationInstruction,
            ForCondInstruction forCondInstruction,
            Label label) {}
}
