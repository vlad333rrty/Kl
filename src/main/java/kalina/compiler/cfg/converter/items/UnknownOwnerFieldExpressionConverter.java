package kalina.compiler.cfg.converter.items;

import java.util.Optional;
import java.util.function.Function;

import kalina.compiler.ast.expression.field.ASTUnknownOwnerFieldExpression;
import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.field.FieldAccessExpression;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class UnknownOwnerFieldExpressionConverter {
    private static final Logger logger = LogManager.getLogger(UnknownOwnerFieldExpressionConverter.class);

    public static Expression convert(
            ASTUnknownOwnerFieldExpression unknownOwnerFieldExpression,
            Type ownerType,
            GetFieldInfoProvider getFieldInfoProvider)
    {
        String ownerClassName = ownerType.getClassName();
        Function<String, Optional<OxmaFieldInfo>> fieldInfoProvider =
                getFieldInfoProvider.getFieldInfoProvider(ownerClassName).orElseThrow();
        String fieldName = unknownOwnerFieldExpression.getFieldName();
        Optional<OxmaFieldInfo> fieldInfoO = fieldInfoProvider.apply(fieldName);
        if (fieldInfoO.isEmpty()) {
            logger.error("Unknown field: " + fieldName);
            throw new IllegalArgumentException();
        }
        OxmaFieldInfo fieldInfo = fieldInfoO.get();
        return new FieldAccessExpression(
                fieldInfo.type(),
                fieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC),
                ownerClassName,
                fieldName
        );
    }
}
