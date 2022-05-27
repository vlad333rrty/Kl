package kalina.compiler.cfg.builder.items;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.cfg.builder.MethodEntryCFGTraverser;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.v2.br.IfCondInstruction;
import kalina.compiler.instructions.v2.br.IfElseEndInstruction;
import kalina.compiler.instructions.v2.br.IfThenEndInstruction;
import org.objectweb.asm.Label;

/**
 * @author vlad333rrty
 */
public class ASTIfProcessor extends AbstractBranchExpressionProcessor<ASTIfInstruction> {
    public ASTIfProcessor(
            AbstractExpressionConverter expressionConverter,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        super(expressionConverter, functionInfoProvider, fieldInfoProvider);
    }
    @Override
    public ThenAndElseNodes process(
            ASTIfInstruction ifInstruction,
            Iterator<ASTExpression> iterator,
            AbstractLocalVariableTable localVariableTable,
            Consumer<Instruction> bbEntryConsumer,
            Consumer<List<Instruction>> blockEndInstructionProvider,
            Consumer<List<Instruction>> blockStartInstructionProvider,
            MethodEntryCFGTraverser traverser) throws CFGConversionException, IncompatibleTypesException
    {
        IfCondInstruction ifCondInstruction = createIfCondInstruction(ifInstruction, localVariableTable);
        bbEntryConsumer.accept(ifCondInstruction);
        Label end = new Label();
        AbstractCFGNode thenNode = traverser.traverseScope(ifInstruction.thenBr(),
                localVariableTable,
                bbs -> bbs.add(new IfThenEndInstruction(ifCondInstruction.getLabel(), end, ifInstruction.elseBr().isPresent())),
                blockStartInstructionProvider);
        Optional<AbstractCFGNode> elseNodeO = ifInstruction.elseBr().isPresent()
                ? Optional.of(traverser.traverseScope(ifInstruction.elseBr().get(),
                localVariableTable,
                bbs -> bbs.add(new IfElseEndInstruction(end)),
                blockStartInstructionProvider))
                : Optional.empty();
        AbstractCFGNode elseNode = elseNodeO.isPresent()
                ? elseNodeO.get()
                : traverser.traverse(iterator, localVariableTable, blockEndInstructionProvider, blockStartInstructionProvider);

        return new ThenAndElseNodes(thenNode, elseNode, Optional.empty());
    }

    private IfCondInstruction createIfCondInstruction(ASTIfInstruction ifInstruction, AbstractLocalVariableTable localVariableTable) throws CFGConversionException {
        return new IfCondInstruction(
                expressionConverter.convertCondExpression(
                        ifInstruction.condExpression(),
                        localVariableTable,
                        functionInfoProvider,
                        fieldInfoProvider
                )
        );
    }
}
