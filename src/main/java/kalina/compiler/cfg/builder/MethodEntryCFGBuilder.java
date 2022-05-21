package kalina.compiler.cfg.builder;

import java.util.List;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class MethodEntryCFGBuilder {
    private static final Logger logger = LogManager.getLogger(MethodEntryCFGBuilder.class);

    private final MethodEntryCFGTraverser traverser;

    public MethodEntryCFGBuilder(MethodEntryCFGTraverser traverser) {
        this.traverser = traverser;
    }

    public AbstractCFGNode build(List<ASTExpression> methodEntry, AbstractLocalVariableTable localVariableTable) throws CFGConversionException, IncompatibleTypesException {
        return traverser.traverse(methodEntry.iterator(), localVariableTable);
    }
}
