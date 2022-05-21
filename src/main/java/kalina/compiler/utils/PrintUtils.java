package kalina.compiler.utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author vlad333rrty
 */
public final class PrintUtils {
    public static String complexExpressionToString(List<?> elements, List<?> operations) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                builder.append(operations.get(i - 1)).append(" ");
            }
            builder.append(elements.get(i).toString()).append(" ");
        }
        return builder.toString();
    }

    public static String listToString(List<?> elements) {
        return elements.stream().map(Objects::toString).collect(Collectors.joining(","));
    }
}
