package kalina.compiler.cfg.traverse;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.cfg.validator.TypesComparator;
import kalina.compiler.cfg.validator.Validator;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.v2.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.ArrayAssignInstruction;
import kalina.compiler.instructions.v2.AssignInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.cfg.data.VariableInfo;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class ExpressionValidator {
    public static void validateInitExpression(InitInstruction instruction) throws IncompatibleTypesException {
        Optional<Type> maxType = TypesComparator.getMax(instruction.getRhs().stream().map(Expression::getType).toList());
        Validator.validateTypesCompatible(instruction.getLhs().getType(), maxType.orElseThrow());
    }

    public static void validateAssignExpression(AbstractAssignInstruction assignInstruction) throws IncompatibleTypesException {
        List<VariableInfo> lhs = assignInstruction.getLhs();
        List<Expression> rhs = assignInstruction.getRhs();
        if (assignInstruction instanceof AssignInstruction) {
            validateAssignExpression(lhs, rhs);
        } else if (assignInstruction instanceof ArrayAssignInstruction) {
            validateArrayAssignExpression(lhs, rhs);
        } else {
            throw new IllegalArgumentException("Unexpected expression " + assignInstruction);
        }
    }

    private static void validateArrayAssignExpression(List<VariableInfo> lhs, List<Expression> rhs) throws IncompatibleTypesException {
        for (int i = 0, lhsSize = lhs.size(); i < lhsSize; i++) {
            VariableInfo variableInfo = lhs.get(i);
            Validator.validateTypesCompatible(variableInfo.getArrayVariableInfoOrElseThrow().getLoweredType(), rhs.get(i).getType());
        }
    }

    private static void validateAssignExpression(List<VariableInfo> lhs, List<Expression> rhs) throws IncompatibleTypesException {
        for (int i = 0, lhsSize = lhs.size(); i < lhsSize; i++) {
            VariableInfo variableInfo = lhs.get(i);
            Validator.validateTypesCompatible(variableInfo.getType(), rhs.get(i).getType());
        }
    }
}
