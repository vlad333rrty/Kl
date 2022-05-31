package kalina.compiler.cfg.builder.instruction;

import java.util.Optional;
import java.util.function.Function;

import kalina.compiler.cfg.common.validation.StaticContextValidator;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.data.TypeChecker;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class StaticScopeInstructionCFGBuilder extends AbstractInstructionCFGBuilder {

    public StaticScopeInstructionCFGBuilder(AbstractExpressionConverter expressionConverter, TypeChecker typeChecker, Type returnType, OxmaFunctionInfoProvider functionInfoProvider, Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider) {
        super(expressionConverter, typeChecker, returnType, functionInfoProvider, fieldInfoProvider);
    }

    @Override
    protected void validateStaticContext(boolean isStatic, String memberName) {
        StaticContextValidator.validateStaticContext(isStatic, memberName);
    }
}
