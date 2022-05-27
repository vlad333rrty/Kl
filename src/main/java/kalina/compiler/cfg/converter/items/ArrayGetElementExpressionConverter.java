package kalina.compiler.cfg.converter.items;

import java.util.List;

import kalina.compiler.ast.expression.array.ASTArrayGetElementExpression;
import kalina.compiler.cfg.common.CFGUtils;
import kalina.compiler.cfg.data.GetVariableOrField;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.TypeAndIndex;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.array.ArrayGetElementExpression;
import kalina.compiler.expressions.v2.field.FieldAccessExpression;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ArrayGetElementExpressionConverter {
    public static Expression convert(
            GetVariableOrField getVariableOrField,
            ASTArrayGetElementExpression getElementExpression,
            List<Expression> convertedIndices)
    {
        String name = getElementExpression.getVariableName();
        GetVariableOrField.VariableOrFieldInfo variableInfo =
                getVariableOrField.getVariableOrFieldInfoOrElseThrow(name);

        if (variableInfo.typeAndIndex.isPresent()) {
            TypeAndIndex typeAndIndex = variableInfo.typeAndIndex.get();
            Type type = typeAndIndex.getType();
            return new ArrayGetElementExpression(
                    convertedIndices,
                    CFGUtils.getArrayElementType(type),
                    CFGUtils.lowArrayDimension(type, getElementExpression.getIndices().size()),
                    type,
                    new VariableExpression(typeAndIndex.getIndex(), type, name),
                    name
            );
        } else {
            OxmaFieldInfo fieldInfo = variableInfo.fieldInfo.get();
            Type type = fieldInfo.type();
            return new ArrayGetElementExpression(
                    convertedIndices,
                    CFGUtils.getArrayElementType(type),
                    CFGUtils.lowArrayDimension(type, getElementExpression.getIndices().size()),
                    type,
                    FieldAccessExpression.fromFieldInfoAndName(fieldInfo, name),
                    name
            );
        }
    }
}
