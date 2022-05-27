package kalina.compiler.cfg.builder.items;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import kalina.compiler.ast.expression.ASTBranchExpression;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.cfg.builder.MethodEntryCFGTraverser;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public abstract class AbstractBranchExpressionProcessor<T extends ASTBranchExpression> {
    protected final AbstractExpressionConverter expressionConverter;
    protected final OxmaFunctionInfoProvider functionInfoProvider;
    protected final Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider;

    public AbstractBranchExpressionProcessor(
            AbstractExpressionConverter expressionConverter,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        this.expressionConverter = expressionConverter;
        this.functionInfoProvider = functionInfoProvider;
        this.fieldInfoProvider = fieldInfoProvider;
    }

    public abstract ThenAndElseNodes process(
            T item,
            Iterator<ASTExpression> iterator,
            AbstractLocalVariableTable localVariableTable,
            Consumer<Instruction> bbEntryConsumer,
            Consumer<List<Instruction>> blockEndInstructionProvider,
            Consumer<List<Instruction>> blockStartInstructionProvider,
            MethodEntryCFGTraverser traverser) throws CFGConversionException, IncompatibleTypesException;
}
