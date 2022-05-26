import java.util.List;

import kalina.compiler.OxmaCompiler;
import kalina.compiler.bb.v2.ClassBasicBlock;

/**
 * @author vlad333rrty
 */
public class NoOptimizationsCompiler extends OxmaCompiler {
    @Override
    protected void performOptimizations(List<ClassBasicBlock> classBasicBlocks) {
        // do nothing
    }
}
