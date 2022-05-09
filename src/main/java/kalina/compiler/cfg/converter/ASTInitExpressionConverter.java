package kalina.compiler.cfg.converter;

import kalina.compiler.ast.expression.array.ASTArrayCreationExpression;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.v2.array.ArrayWithCapacityCreationExpression;
import kalina.compiler.syntax.parser.data.AbstractLocalVariableTable;
import kalina.compiler.syntax.parser.data.ArrayVariableInfo;
import kalina.compiler.syntax.parser.data.ExtendedVariableInfo;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;

/**
 * @author vlad333rrty
 */
public class ASTInitExpressionConverter {
    private final ASTExpressionConverter astExpressionConverter;

    public ASTInitExpressionConverter(ASTExpressionConverter astExpressionConverter) {
        this.astExpressionConverter = astExpressionConverter;
    }

    public Expression convert(
            ASTExpression astExpression,
            AbstractLocalVariableTable localVariableTable,
            OxmaFunctionTable functionTable,
            String varName)
    {
        if (astExpression instanceof ASTArrayCreationExpression arrayCreationExpression) {
            ExtendedVariableInfo variableInfo = localVariableTable.findVariableOrElseThrow(varName);
            variableInfo.setArrayVariableInfo(new ArrayVariableInfo(
                    arrayCreationExpression.getCapacities(),
                    arrayCreationExpression.getElementType()
            ));

            return new ArrayWithCapacityCreationExpression(
                    arrayCreationExpression.getCapacities(),
                    arrayCreationExpression.getArrayType(),
                    arrayCreationExpression.getElementType());
        } else {
            return astExpressionConverter.convert(astExpression, localVariableTable, functionTable);
        }
    }
}
