package kalina.compiler.cfg.converter;

import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;

/**
 * @author vlad333rrty
 */
public final class ExpressionConverterFactory {
    public static AbstractExpressionConverter createForStaticScope(
            GetFunctionInfoProvider getFunctionInfoProvider,
            GetFieldInfoProvider getFieldInfoProvider)
    {
        return new StaticScopeExpressionConverter(getFunctionInfoProvider, getFieldInfoProvider);
    }

    public static AbstractExpressionConverter createForNonStaticScope(
            GetFunctionInfoProvider getFunctionInfoProvider,
            GetFieldInfoProvider getFieldInfoProvider)
    {
        return new ExpressionConverter(getFunctionInfoProvider, getFieldInfoProvider);
    }

    public static AbstractExpressionConverter createExpressionConverter(
            GetFunctionInfoProvider getFunctionInfoProvider,
            GetFieldInfoProvider getFieldInfoProvider,
            boolean isStaticScope)
    {
        return isStaticScope
                ? createForStaticScope(getFunctionInfoProvider, getFieldInfoProvider)
                : createForNonStaticScope(getFunctionInfoProvider, getFieldInfoProvider);
    }
}
