package kalina.compiler.cfg.builder;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.cfg.validator.TypesComparator;
import kalina.compiler.cfg.validator.Validator;
import kalina.compiler.expressions.Expression;
import kalina.compiler.instructions.v2.assign.AbstractAssignInstruction;
import kalina.compiler.instructions.v2.assign.ArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.assign.AssignInstruction;
import kalina.compiler.instructions.v2.assign.FieldArrayElementAssignInstruction;
import kalina.compiler.instructions.v2.assign.FieldAssignInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.cfg.data.VariableInfo;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class ExpressionValidator {
    public static void validateInitExpression(InitInstruction instruction) throws IncompatibleTypesException {
        if (instruction.getRhs().isEmpty()) {
            return;
        }
        Optional<Type> maxType = TypesComparator.getMax(instruction.getRhs().stream().map(Expression::getType).toList());
        Validator.validateTypesCompatible(maxType.orElseThrow(), instruction.getLhs().getType());
    }

    public static void validateAssignExpression(AbstractAssignInstruction assignInstruction) throws IncompatibleTypesException {
        List<VariableInfo> lhs = assignInstruction.getLhs();
        List<Expression> rhs = assignInstruction.getRhs();
        if (assignInstruction instanceof AssignInstruction || assignInstruction instanceof FieldAssignInstruction) {
            validateAssignExpression(lhs, rhs);
        } else if (assignInstruction instanceof ArrayElementAssignInstruction || assignInstruction instanceof FieldArrayElementAssignInstruction) {
            validateArrayAssignExpression(lhs, rhs);
        } else {
            throw new IllegalArgumentException("Unexpected expression " + assignInstruction);
        }
    }

    private static void validateArrayAssignExpression(List<VariableInfo> lhs, List<Expression> rhs) throws IncompatibleTypesException {
        for (int i = 0, lhsSize = lhs.size(); i < lhsSize; i++) {
            VariableInfo variableInfo = lhs.get(i);
            Validator.validateTypesCompatible(rhs.get(i).getType(), variableInfo.getArrayVariableInfoOrElseThrow().getLoweredType());
        }
    }

    private static void validateAssignExpression(List<VariableInfo> lhs, List<Expression> rhs) throws IncompatibleTypesException {
        for (int i = 0, lhsSize = lhs.size(); i < lhsSize; i++) {
            VariableInfo variableInfo = lhs.get(i);
            Validator.validateTypesCompatible(rhs.get(i).getType(), variableInfo.getType());
        }
    }
}
