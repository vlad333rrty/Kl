package kalina.compiler.cfg.builder.items;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import kalina.compiler.ast.ASTBranchExpressionConversionFactory;
import kalina.compiler.ast.expression.ASTBranchExpression;
import kalina.compiler.ast.expression.ASTDoInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTForInstruction;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.cfg.builder.MethodEntryCFGTraverser;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public class BranchExpressionConverter {
    private final ASTIfProcessor ifProcessor;
    private final ASTForProcessor forProcessor;
    private final ASTDoProcessor doProcessor;

    public BranchExpressionConverter(ASTIfProcessor ifProcessor, ASTForProcessor forProcessor, ASTDoProcessor doProcessor) {
        this.ifProcessor = ifProcessor;
        this.forProcessor = forProcessor;
        this.doProcessor = doProcessor;
    }

    public ThenAndElseNodes convertBranchExpression(
            ASTBranchExpression branchExpression,
            Iterator<ASTExpression> iterator,
            AbstractLocalVariableTable localVariableTable,
            Consumer<Instruction> bbEntryConsumer,
            Consumer<List<Instruction>> blockEndInstructionProvider,
            Consumer<List<Instruction>> blockStartInstructionProvider,
            MethodEntryCFGTraverser traverser) throws CFGConversionException, IncompatibleTypesException
    {
        return branchExpression.convert(new ASTBranchExpressionConversionFactory<>() {
            @Override
            public ThenAndElseNodes convertFor(ASTForInstruction forInstruction) throws CFGConversionException, IncompatibleTypesException {
                return forProcessor.process(forInstruction, iterator, localVariableTable, bbEntryConsumer, blockEndInstructionProvider, blockStartInstructionProvider, traverser);
            }

            @Override
            public ThenAndElseNodes convertDo(ASTDoInstruction doInstruction) throws CFGConversionException, IncompatibleTypesException {
                return doProcessor.process(doInstruction, iterator, localVariableTable, bbEntryConsumer, blockEndInstructionProvider, blockStartInstructionProvider, traverser);
            }

            @Override
            public ThenAndElseNodes convertIf(ASTIfInstruction ifInstruction) throws CFGConversionException, IncompatibleTypesException {
                return ifProcessor.process(ifInstruction, iterator, localVariableTable, bbEntryConsumer, blockEndInstructionProvider, blockStartInstructionProvider, traverser);
            }
        });
    }
}
