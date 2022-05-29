package kalina.compiler.codegen.typeCast;

import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class TypeCastValidator {
    private static final Map<Integer, Set<Integer>> possibleCasts = Map.of(
            Type.INT, Set.of(Type.LONG, Type.FLOAT, Type.DOUBLE),
            Type.LONG, Set.of(Type.INT, Type.FLOAT, Type.DOUBLE),
            Type.FLOAT, Set.of(Type.DOUBLE),
            Type.DOUBLE, Set.of(Type.FLOAT)
    );

    public static boolean canCast(Type from, Type to) {
        return possibleCasts.containsKey(from.getSort()) && possibleCasts.get(from.getSort()).contains(to.getSort());
    }
}
