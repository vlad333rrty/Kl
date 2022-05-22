package kalina.compiler.cfg.converter.items;

import kalina.compiler.ast.expression.ASTVariableExpression;
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

    public static Expression convert(GetVariableOrField getVariableOrField, ASTVariableExpression variableExpression) {
        GetVariableOrField.VariableOrFieldInfo variableOrFieldInfo =
                getVariableOrField.getVariableOrFieldInfoOrElseThrow(variableExpression.name());
        if (variableOrFieldInfo.typeAndIndex.isPresent()) {
            TypeAndIndex typeAndIndex = variableOrFieldInfo.typeAndIndex.get();
            return new VariableExpression(typeAndIndex.getIndex(), typeAndIndex.getType(), variableExpression.name());
        }
        OxmaFieldInfo fieldInfo = variableOrFieldInfo.fieldInfo.get();
        return new FieldAccessExpression(
                fieldInfo.type(),
                fieldInfo.modifiers().contains(ClassEntryUtils.Modifier.STATIC),
                fieldInfo.ownerClassName(),
                variableExpression.name()
        );
    }
}
