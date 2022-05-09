package kalina.compiler.cfg.validator;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class TypesComparator {
    private static final Logger logger = LogManager.getLogger(TypesComparator.class);

    private static final Map<String, Integer> typeToWeight = Map.of(
            "short", 0,
            "int", 1,
            "long", 2,
            "float", 2,
            "double", 3,
            "java.lang.String", 4
    );

    public static Type getMax(Type type1, Type type2) throws IncompatibleTypesException {
        Optional<Integer> weight1 = Optional.ofNullable(typeToWeight.get(type1.getClassName()));
        Optional<Integer> weight2 = Optional.ofNullable(typeToWeight.get(type2.getClassName()));
        if (weight1.isEmpty() || weight2.isEmpty()) {
            if (type1.equals(type2)) {
                return type1;
            }
            throw new IncompatibleTypesException("Cannot cast " + type1.getClassName() + " to " + type2.getClassName());
        }
        return weight1.get() > weight2.get() ? type1 : type2;
    }

    public static Optional<Type> getMax(List<Type> types) throws IncompatibleTypesException {
        if (types.isEmpty()) {
            logger.warn("Empty types list. No max value can be found");
            return Optional.empty();
        }
        Type max = types.stream().findFirst().get();
        for (Type type : types) {
            if (!TypesComparator.getMax(max, type).equals(max)) {
                max = type;
            }
        }

        return Optional.of(max);
    }
}
