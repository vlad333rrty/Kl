package kalina.compiler.instructions.v2;

import java.util.List;

import kalina.compiler.cfg.data.SSAVariableInfo;

/**
 * @author vlad333rrty
 */
public interface WithLHS {
    List<SSAVariableInfo> getVariableInfos();
}
