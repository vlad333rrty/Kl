package kalina.compiler.cfg.converter.items;

import java.util.Optional;
import java.util.function.Function;

import kalina.compiler.ast.expression.field.ASTOtherFieldAccessExpression;
import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetVariableOrField;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.TypeAndIndex;
import kalina.compiler.expressions.v2.field.OtherFieldAccessExpression;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;

/**
 * @author vlad333rrty
 */
public class OtherFieldExpressionsConverter {

    public static OtherFieldAccessExpression convert(
            ASTOtherFieldAccessExpression otherFieldAccessExpression,
            GetVariableOrField getVariableOrField,
            GetFieldInfoProvider getFieldInfoProvider)
    {
        String ownerObjectName = otherFieldAccessExpression.getOwnerName();
        String fieldName = otherFieldAccessExpression.getFieldName();
        Optional<GetVariableOrField.VariableOrFieldInfo> variableOrFieldInfoO = getVariableOrField.getVariableOrFieldInfo(ownerObjectName);
        if (variableOrFieldInfoO.isPresent()) {
            GetVariableOrField.VariableOrFieldInfo variableOrFieldInfo = variableOrFieldInfoO.get();
            if (variableOrFieldInfo.typeAndIndex.isPresent()) {
                TypeAndIndex typeAndIndex = variableOrFieldInfo.typeAndIndex.get();
                String ownerClassName = typeAndIndex.getType().getClassName();
                OxmaFieldInfo otherFieldInfo = getOtherFieldInfo(getFieldInfoProvider, ownerClassName, ownerObjectName, fieldName);
                return new OtherFieldAccessExpression(
                        otherFieldInfo.type(),
                        otherFieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC),
                        ownerClassName,
                        fieldName,
                        typeAndIndex.getIndex()
                );
            } else {
                OxmaFieldInfo fieldInfo = variableOrFieldInfo.fieldInfo.get();
                String ownerClassName = fieldInfo.type().getClassName();
                OxmaFieldInfo otherFieldInfo = getOtherFieldInfo(
                        getFieldInfoProvider,
                        ownerClassName,
                        ownerObjectName,
                        fieldName);
                return new OtherFieldAccessExpression(
                        otherFieldInfo.type(),
                        otherFieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC),
                        ownerClassName,
                        fieldName);
            }
        } else {
            OxmaFieldInfo otherFieldInfo = getOtherFieldInfo(getFieldInfoProvider, ownerObjectName, ownerObjectName, fieldName);
            return new OtherFieldAccessExpression(
                    otherFieldInfo.type(),
                    otherFieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC),
                    ownerObjectName,
                    fieldName
            );
        }
    }


    private static OxmaFieldInfo getOtherFieldInfo(
            GetFieldInfoProvider getFieldInfoProvider,
            String ownerClassName,
            String ownerObjectName,
            String fieldName)
    {
        Optional<Function<String, Optional<OxmaFieldInfo>>> otherFieldInfoProviderO =
                getFieldInfoProvider.getFieldInfoProvider(ownerClassName);
        if (otherFieldInfoProviderO.isEmpty()) {
            throw new IllegalArgumentException(String
                    .format("Unidentified reference: %s.%s", ownerObjectName, fieldName));
        }
        Function<String, Optional<OxmaFieldInfo>> otherFieldInfoProvider = otherFieldInfoProviderO.get();
        Optional<OxmaFieldInfo> otherFieldInfoO = otherFieldInfoProvider.apply(fieldName);
        if (otherFieldInfoO.isEmpty()) {
            throw new IllegalArgumentException(String
                    .format("Unidentified reference: %s.%s", ownerObjectName, fieldName));
        }
        return otherFieldInfoO.get();
    }

}
