package kalina.compiler.cfg.converter.items;

import java.util.Optional;

import kalina.compiler.cfg.data.GetVariableOrField;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.TypeAndIndex;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.field.FieldAccessExpression;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;

/**
 * @author vlad333rrty
 */
public class VariableOrFieldExpressionConverter {

    public static Optional<Expression> convert(GetVariableOrField getVariableOrField, String varName) {
        Optional<GetVariableOrField.VariableOrFieldInfo> variableOrFieldInfoO =
                getVariableOrField.getVariableOrFieldInfo(varName);
        if (variableOrFieldInfoO.isEmpty()) {
            return Optional.empty();
        }
        GetVariableOrField.VariableOrFieldInfo variableOrFieldInfo = variableOrFieldInfoO.get();
        if (variableOrFieldInfo.typeAndIndex.isPresent()) {
            TypeAndIndex typeAndIndex = variableOrFieldInfo.typeAndIndex.get();
            return Optional.of(new VariableExpression(typeAndIndex.getIndex(), typeAndIndex.getType(), varName));
        }
        OxmaFieldInfo fieldInfo = variableOrFieldInfo.fieldInfo.get();
        return Optional.of(new FieldAccessExpression(
                fieldInfo.type(),
                fieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC),
                fieldInfo.ownerClassName(),
                varName
        ));
    }
}
