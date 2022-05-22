package kalina.compiler.cfg.builder.items;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import kalina.compiler.ast.expression.ASTDoInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.cfg.builder.MethodEntryCFGTraverser;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.br.DoBlockBeginInstruction;
import kalina.compiler.instructions.v2.br.DoBlockEndInstruction;

/**
 * @author vlad333rrty
 */
public class ASTDoProcessor extends AbstractBranchExpressionProcessor<ASTDoInstruction> {
    public ASTDoProcessor(
            AbstractExpressionConverter expressionConverter,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        super(expressionConverter, functionInfoProvider, fieldInfoProvider);
    }

    @Override
    public ThenAndElseNodes process(
            ASTDoInstruction item,
            Iterator<ASTExpression> iterator,
            AbstractLocalVariableTable localVariableTable,
            Consumer<Instruction> bbEntryConsumer,
            Consumer<List<Instruction>> blockEndInstructionProvider,
            MethodEntryCFGTraverser traverser) throws CFGConversionException, IncompatibleTypesException
    {
        CondExpression condExpression = expressionConverter.convertCondExpression(
                item.condition(),
                localVariableTable,
                functionInfoProvider,
                fieldInfoProvider
        );
        DoBlockBeginInstruction doBlockBeginInstruction = new DoBlockBeginInstruction(condExpression.getLabel());
        bbEntryConsumer.accept(doBlockBeginInstruction);

        DoBlockEndInstruction doBlockEndInstruction = new DoBlockEndInstruction(CondExpression.negate(condExpression));
        AbstractCFGNode thenNode = traverser.traverseScope(
                item.entry(),
                localVariableTable,
                bbs -> bbs.add(doBlockEndInstruction)
        );

        AbstractCFGNode elseNode = traverser.traverse(iterator, localVariableTable, blockEndInstructionProvider);
        return new ThenAndElseNodes(thenNode, elseNode);
    }
}
