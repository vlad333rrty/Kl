package kalina.compiler.cfg.validator;

import java.util.List;
import java.util.Optional;

import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.codegen.typeCast.TypeCastOpcodesMapper;
import kalina.compiler.expressions.Expression;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 *
 * todo consider cond operations overriding in classses
 *
 */
public class Validator {

    // todo consider inheritance
    public static void validateTypesCompatible(Type from, Type to) throws IncompatibleTypesException {
        boolean isValid =  from.equals(to)
                || (TypeChecker.isPrimitiveNumber(from.getClassName())
                && TypeChecker.isPrimitiveNumber(to.getClassName())
                && TypeCastOpcodesMapper.canCast(from, to));
        if (!isValid) {
            throw new IncompatibleTypesException(String.format("Incompatible types: %s and %s", from.getClassName(), to.getClassName()));
        }
    }

    public static void validateConditionExpression(List<Type> types) throws IncompatibleTypesException {
        Optional<Type> max = TypesComparator.getMax(types);
        if (max.isEmpty() || max.get().equals(Type.getType(String.class))) {
            throw new IncompatibleTypesException("Incompatible types for comparison operation");
        }
    }

    public static void validateArrayIndices(List<Expression> indices, List<Integer> capacities) {
        if (indices.size() > capacities.size()) {
            throw new IllegalArgumentException("Incompatible array dimensions");
        }
    }
}
