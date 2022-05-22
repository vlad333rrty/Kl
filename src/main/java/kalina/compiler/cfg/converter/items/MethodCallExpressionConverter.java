package kalina.compiler.cfg.converter.items;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.method.ASTMethodCallExpression;
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;
import kalina.compiler.cfg.data.GetVariableOrField;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.OxmaFunctionInfo;
import kalina.compiler.cfg.data.TypeAndIndex;
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.field.FieldAccessExpression;
import kalina.compiler.expressions.v2.funCall.FunCallExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class MethodCallExpressionConverter {
    private static final Logger logger = LogManager.getLogger(MethodCallExpressionConverter.class);

    public static Expression convert(
            ASTMethodCallExpression methodCallExpression,
            GetVariableOrField getVariableOrField,
            GetFunctionInfoProvider getFunctionInfoProvider,
            BiFunction<ASTExpression, OxmaFunctionInfoProvider, Expression> convertFun)
    {
        String ownerObjectName = methodCallExpression.ownerObjectName();
        Optional<GetVariableOrField.VariableOrFieldInfo> variableO = getVariableOrField.getVariableOrFieldInfo(ownerObjectName);
        FunInfoProviderAndVarAccessExpr funInfoProviderAndVarAccessExpr =
                getFunInfoProviderAndVarAccessExpr(variableO, ownerObjectName, getFunctionInfoProvider);
        Optional<OxmaFunctionInfoProvider> otherClassFunctionTable = funInfoProviderAndVarAccessExpr.otherClassFunctionTable;
        if (otherClassFunctionTable.isEmpty()) {
            logger.error("Unknown type {}", methodCallExpression.ownerObjectName());
            throw new IllegalArgumentException("No function info can be found for " + methodCallExpression.funName());
        }

        List<Expression> arguments = methodCallExpression.arguments().stream()
                .map(arg -> convertFun.apply(arg, otherClassFunctionTable.get()))
                .toList();
        String funName = methodCallExpression.funName();
        Optional<OxmaFunctionInfo> functionInfo = otherClassFunctionTable.get()
                .getFunctionInfo(funName, CFGUtils.getSignatureFromExpressions(arguments));
        if (functionInfo.isEmpty()) {
            logger.error("No declaration found for method {}", funName);
            throw new IllegalArgumentException("No declaration found for method " + funName);
        }
        return new FunCallExpression(funName, arguments, functionInfo.get(), funInfoProviderAndVarAccessExpr.variableAccessExpression);
    }

    private static FunInfoProviderAndVarAccessExpr getFunInfoProviderAndVarAccessExpr(
            Optional<GetVariableOrField.VariableOrFieldInfo> variableOrFieldInfoO,
            String ownerObjectName,
            GetFunctionInfoProvider getFunctionInfoProvider)
    {
        final Optional<OxmaFunctionInfoProvider> otherClassFunctionTable;
        Expression expr = null;
        if (variableOrFieldInfoO.isPresent()) {
            GetVariableOrField.VariableOrFieldInfo variableOrFieldInfo = variableOrFieldInfoO.get();
            if (variableOrFieldInfo.typeAndIndex.isPresent()) {
                TypeAndIndex typeAndIndex = variableOrFieldInfo.typeAndIndex.get();
                otherClassFunctionTable = getFunctionInfoProvider.getFunctionTable(typeAndIndex.getType().getClassName());
                expr = new VariableExpression(typeAndIndex.getIndex(), typeAndIndex.getType(), null);
            } else {
                OxmaFieldInfo fieldInfo = variableOrFieldInfo.fieldInfo.get();
                otherClassFunctionTable = getFunctionInfoProvider.getFunctionTable(fieldInfo.ownerClassName());
                expr = FieldAccessExpression.fromFieldInfoAndName(fieldInfo, ownerObjectName);
            }
        } else {
            otherClassFunctionTable = getFunctionInfoProvider.getFunctionTable(ownerObjectName);
        }

        return new FunInfoProviderAndVarAccessExpr(otherClassFunctionTable, Optional.ofNullable(expr));
    }

    private record FunInfoProviderAndVarAccessExpr(
            Optional<OxmaFunctionInfoProvider> otherClassFunctionTable,
            Optional<Expression> variableAccessExpression)
    {
    }
}
