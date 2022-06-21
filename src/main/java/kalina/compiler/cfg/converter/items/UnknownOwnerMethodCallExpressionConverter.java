package kalina.compiler.cfg.converter.items;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.method.ASTUnknownOwnerMethodCall;
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;
import kalina.compiler.cfg.data.OxmaFunctionInfo;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.funCall.FunCallExpression;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class UnknownOwnerMethodCallExpressionConverter extends UnknownOwnerExpressionConverterBase {
    private static final Logger logger = LogManager.getLogger(UnknownOwnerMethodCallExpressionConverter.class);

    public static Expression convert(
            ASTUnknownOwnerMethodCall unknownOwnerMethodCall,
            Type ownerType,
            GetFunctionInfoProvider getFunctionInfoProvider,
            BiFunction<ASTExpression, OxmaFunctionInfoProvider, Expression> convertFun)
    {
        validateOwnerType(ownerType);
        OxmaFunctionInfoProvider functionInfoProvider =
                getFunctionInfoProvider.getFunctionTable(ownerType.getClassName()).orElseThrow();

        String funName = unknownOwnerMethodCall.getFunName();
        List<Expression> arguments = unknownOwnerMethodCall.getArguments().stream()
                .map(arg -> convertFun.apply(arg, functionInfoProvider))
                .toList();
        List<Type> signature = CFGUtils.getSignatureFromExpressions(arguments);
        Optional<OxmaFunctionInfo> functionInfoO = functionInfoProvider.getFunctionInfo(funName, signature);
        if (functionInfoO.isEmpty()) {
            logger.error("No declaration found for method {}", funName);
            throw new IllegalArgumentException();
        }
        OxmaFunctionInfo functionInfo = functionInfoO.get();
        if (functionInfo.accessModifier() == ClassEntryUtils.AccessModifier.PRIVATE) {
            throw new IllegalArgumentException("Cannot access private member " + funName
                    + " of the class " + ownerType.getClassName());
        }

        return new FunCallExpression(funName, arguments, functionInfo, Optional.empty());
    }
}
