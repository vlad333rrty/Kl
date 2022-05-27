import kalina.compiler.OxmaCompiler;
import kalina.compiler.cfg.ControlFlowGraph;

/**
 * @author vlad333rrty
 */
public class NoOptimizationsCompiler extends OxmaCompiler {

    @Override
    protected void performOptimizations(ControlFlowGraph controlFlowGraph) {

    }

    @Override
    protected void buildSSAForm(ControlFlowGraph controlFlowGraph) {

    }
}
