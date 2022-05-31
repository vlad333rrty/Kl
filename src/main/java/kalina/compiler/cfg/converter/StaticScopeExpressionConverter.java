package kalina.compiler.cfg.converter;

import kalina.compiler.cfg.common.validation.StaticContextValidator;
import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;

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
    protected void validateStaticContext(boolean isStatic, String memberName) {
        StaticContextValidator.validateStaticContext(isStatic, memberName);
    }
}
