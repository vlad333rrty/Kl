import kalina.compiler.OxmaCompiler;
import kalina.compiler.cfg.ControlFlowGraph;
import kalina.compiler.cfg.optimizations.OptimizationManager;
import kalina.compiler.cfg.optimizations.OptimizationManagerFactory;
import kalina.compiler.cfg.ssa.SSAFormBuilder;

/**
 * @author vlad333rrty
 */
public class OxmaMain extends OxmaCompiler {

    public OxmaMain() {
    }

    public OxmaMain(OxmaCompilerSettings compilerSettings) {
        super(compilerSettings);
    }

    @Override
    protected void performOptimizations(ControlFlowGraph controlFlowGraph) {
        OptimizationManager optimizationManager = OptimizationManagerFactory.create(controlFlowGraph);
        optimizationManager.optimize();
    }

    @Override
    protected void buildSSAForm(ControlFlowGraph controlFlowGraph) {
        SSAFormBuilder ssaFormBuilder = new SSAFormBuilder();
        ssaFormBuilder.buildSSA(controlFlowGraph);
    }
}
