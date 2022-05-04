package kalina.compiler.cfg.converter;

import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.expression.ASTArithmeticExpression;
import kalina.compiler.ast.expression.ASTCondExpression;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTFactor;
import kalina.compiler.ast.expression.ASTFunCallExpression;
import kalina.compiler.ast.expression.ASTObjectCreationExpression;
import kalina.compiler.ast.expression.ASTTerm;
import kalina.compiler.ast.expression.ASTValueExpression;
import kalina.compiler.ast.expression.ASTVariableExpression;
import kalina.compiler.expressions.ArithmeticExpression;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.Factor;
import kalina.compiler.expressions.ObjectCreationExpression;
import kalina.compiler.expressions.Term;
import kalina.compiler.expressions.ValueExpression;
import kalina.compiler.expressions.VariableExpression;
import kalina.compiler.expressions.v2.FunCallExpression;
import kalina.compiler.syntax.parser.data.AbstractLocalVariableTable;
import kalina.compiler.syntax.parser.data.TypeAndIndex;
import kalina.compiler.syntax.parser2.data.OxmaFunctionInfo;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ASTExpressionConverter {
    private static final Logger logger = LogManager.getLogger(ASTExpressionConverter.class);

    public Expression convert(
            ASTExpression astExpression,
            AbstractLocalVariableTable localVariableTable,
            OxmaFunctionTable functionTable)
    {
        if (astExpression instanceof ASTValueExpression valueExpression) {
            return new ValueExpression(valueExpression.value(), valueExpression.type());
        }
        if (astExpression instanceof ASTVariableExpression variableExpression) {
            Optional<TypeAndIndex> typeAndIndexO = localVariableTable.findVariable(variableExpression.name());
            if (typeAndIndexO.isEmpty()) {
                logger.error("No declaration found for variable {}", variableExpression.name());
                return null;
            }
            TypeAndIndex typeAndIndex = typeAndIndexO.get();
            return new VariableExpression(typeAndIndex.getIndex(), typeAndIndex.getType());
        }
        if (astExpression instanceof ASTFactor factor) {
            Expression expression = convert(factor.expression(), localVariableTable, functionTable);
            return factor.shouldNegate() ? Factor.createNegateFactor(expression) : Factor.createFactor(expression);
        }
        if (astExpression instanceof ASTTerm term) {
            List<Factor> factors = term.factors().stream()
                    .map(factor -> (Factor)convert(factor, localVariableTable, functionTable)).toList();
            return new Term(factors, term.operations());
        }
        if (astExpression instanceof ASTArithmeticExpression arithmeticExpression) {
            List<Term> terms = arithmeticExpression.terms().stream()
                    .map(term -> (Term)convert(term, localVariableTable, functionTable)).toList();
            return new ArithmeticExpression(terms, arithmeticExpression.operations());
        }
        if (astExpression instanceof ASTFunCallExpression funCallExpression) {
            List<Expression> arguments = funCallExpression.arguments().stream()
                    .map(arg -> convert(arg, localVariableTable, functionTable))
                    .toList();
            List<Type> signature = arguments.stream().map(Expression::getType).toList();
            Optional<OxmaFunctionInfo> functionInfo =
                    functionTable.getFunctionInfo(funCallExpression.funName(), signature);
            if (functionInfo.isEmpty()) {
                logger.error("No declaration found for method {}", funCallExpression.funName());
                return null;
            }
            return new FunCallExpression(funCallExpression.funName(), arguments, functionInfo.get());
        }
        if (astExpression instanceof ASTObjectCreationExpression objectCreationExpression) {
            List<Expression> arguments = objectCreationExpression.arguments().stream()
                    .map(arg -> convert(arg, localVariableTable, functionTable))
                    .toList();
            return new ObjectCreationExpression(objectCreationExpression.className(), arguments);
        }

        logger.error("Unknown expression {}", astExpression);
        throw new IllegalArgumentException("Unexpected expression type");
    }

    public CondExpression convertCondExpression(
            ASTCondExpression condExpression,
            AbstractLocalVariableTable localVariableTable,
            OxmaFunctionTable functionTable)
    {
        List<Expression> expressions = condExpression.expressions().stream()
                .map(expr -> convert(expr, localVariableTable, functionTable))
                .toList();
        return new CondExpression(expressions, condExpression.operations());
    }
}
