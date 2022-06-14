package kalina.compiler.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author vlad333rrty
 */
public class PerformanceMeasurer {
    private final Map<String, List<Long>> measurementsPerName = new HashMap<>();

    public <T> T measure(Supplier<T> function, String name) {
        long start = System.nanoTime();
        T result = function.get();
        long end = System.nanoTime();
        measurementsPerName.computeIfAbsent(name, k -> new ArrayList<>()).add(end - start);
        return result;
    }

    public void measure(Runnable function, String name) {
        long start = System.nanoTime();
        function.run();
        long end = System.nanoTime();
        measurementsPerName.computeIfAbsent(name, k -> new ArrayList<>()).add(end - start);
    }

    public List<Long> getMeasurementsForName(String name) {
        return measurementsPerName.getOrDefault(name, List.of());
    }
}
