package kalina.compiler.instructions.v2.assign;

import java.util.List;

import kalina.compiler.cfg.data.AssignArrayVariableInfo;

/**
 * @author vlad333rrty
 */
public interface ArrayElementAssign {
    List<AssignArrayVariableInfo> getAssignArrayVariableInfo();
}
