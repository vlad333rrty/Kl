import kalina.compiler.OxmaCompiler;
import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.optimizations.OptimizationManager;
import kalina.compiler.cfg.optimizations.OptimizationManagerFactory;
import kalina.compiler.cfg.ssa.SSAFormBuilder;

/**
 * @author vlad333rrty
 */
public class DCETestCompiler extends OxmaCompiler {

    public DCETestCompiler() {
        super(new Settings.Builder().setShouldPerformOptimizations(true).build());
    }

    @Override
    protected void performOptimizations(ControlFlowGraph controlFlowGraph) {
        OptimizationManager optimizationManager = OptimizationManagerFactory.create(controlFlowGraph);
        optimizationManager.optimize();
    }

    @Override
    protected void buildSSAForm(ControlFlowGraph controlFlowGraph) {
        SSAFormBuilder formBuilder = new SSAFormBuilder();
        formBuilder.buildSSA(controlFlowGraph);
    }
}
