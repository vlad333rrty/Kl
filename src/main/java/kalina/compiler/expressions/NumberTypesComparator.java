package kalina.compiler.expressions;

import java.util.Map;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class NumberTypesComparator {
    private static final Map<String, Integer> typesScores = Map.of(
            "short", 0,
            "int", 1,
            "long", 2,
            "float", 2,
            "double", 3
    );

    public static Type max(Type t1, Type t2) {
        return typesScores.get(t1.getClassName()) > typesScores.get(t2.getClassName()) ? t1 : t2;
    }

    public static boolean isNumber(Type type) {
        return typesScores.containsKey(type.getClassName());
    }
}
