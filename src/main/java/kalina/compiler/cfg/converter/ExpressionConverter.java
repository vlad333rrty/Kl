package kalina.compiler.cfg.converter;

import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;
import kalina.compiler.cfg.data.OxmaFunctionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class ExpressionConverter extends AbstractExpressionConverter {
    private static final Logger logger = LogManager.getLogger(ExpressionConverter.class);

    public ExpressionConverter(GetFunctionInfoProvider getFunctionInfoProvider, GetFieldInfoProvider getFieldInfoProvider) {
        super(getFunctionInfoProvider, getFieldInfoProvider);
    }

    @Override
    protected void validateStaticContext(OxmaFunctionInfo functionInfo, String funName) {
        // do nothing
    }
}
