package kalina.compiler.cfg.builder.instruction;

import java.util.Optional;
import java.util.function.Function;

import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.data.TypeChecker;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class InstructionCFGBuilderFactory {
    private static AbstractInstructionCFGBuilder createForStatic(
            AbstractExpressionConverter expressionConverter,
            TypeChecker typeChecker,
            Type returnType,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        return new StaticScopeInstructionCFGBuilder(expressionConverter, typeChecker, returnType, functionInfoProvider, fieldInfoProvider);
    }

    private static AbstractInstructionCFGBuilder createForNonStatic(
            AbstractExpressionConverter expressionConverter,
            TypeChecker typeChecker,
            Type returnType,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        return new InstructionCFGBuilder(expressionConverter, typeChecker, returnType, functionInfoProvider, fieldInfoProvider);
    }


    public static AbstractInstructionCFGBuilder createInstructionCFGBuilder(
            boolean isStaticContext,
            AbstractExpressionConverter expressionConverter,
            TypeChecker typeChecker,
            Type returnType,
            OxmaFunctionInfoProvider functionInfoProvider,
            Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider)
    {
        return isStaticContext
                ? createForStatic(expressionConverter, typeChecker, returnType, functionInfoProvider, fieldInfoProvider)
                : createForNonStatic(expressionConverter, typeChecker, returnType, functionInfoProvider, fieldInfoProvider);
    }
}
