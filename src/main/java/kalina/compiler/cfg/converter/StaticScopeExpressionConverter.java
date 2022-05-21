package kalina.compiler.cfg.converter;

import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;
import kalina.compiler.cfg.data.OxmaFunctionInfo;

/**
 * @author vlad333rrty
 */
public class StaticScopeExpressionConverter extends AbstractExpressionConverter {

    public StaticScopeExpressionConverter(
            GetFunctionInfoProvider getFunctionInfoProvider,
            GetFieldInfoProvider getFieldInfoProvider)
    {
        super(getFunctionInfoProvider, getFieldInfoProvider);
    }

    @Override
    protected void validateStaticContext(OxmaFunctionInfo functionInfo, String funName) {
        if (!functionInfo.isStatic()) {
            throw new IllegalArgumentException("Cannot call function from the static context: " + funName);
        }
    }
}
