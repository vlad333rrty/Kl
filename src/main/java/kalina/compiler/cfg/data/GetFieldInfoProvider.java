package kalina.compiler.cfg.data;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author vlad333rrty
 */
public class GetFieldInfoProvider {
    private final Map<String, Function<String, Optional<OxmaFieldInfo>>> classNameToFieldInfoProvider;

    public GetFieldInfoProvider(Map<String, Function<String, Optional<OxmaFieldInfo>>> classNameToFieldInfoProvider) {
        this.classNameToFieldInfoProvider = classNameToFieldInfoProvider;
    }

    public Optional<Function<String, Optional<OxmaFieldInfo>>> getFieldInfoProvider(String className) {
        return Optional.ofNullable(classNameToFieldInfoProvider.get(className));
    }
}
