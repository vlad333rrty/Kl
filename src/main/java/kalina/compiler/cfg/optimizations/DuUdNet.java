package kalina.compiler.cfg.optimizations;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author vlad333rrty
 */
public class DuUdNet {
    private final Map<Definition, List<UseCoordinates>> du;
    private final Map<Use, List<DefinitionCoordinates>> ud;

    public DuUdNet(Map<Definition, List<UseCoordinates>> du, Map<Use, List<DefinitionCoordinates>> ud) {
        this.du = du;
        this.ud = ud;
    }

    public Function<Definition, List<UseCoordinates>> getDuChainProvider() {
        return du::get;
    }

    public Function<Use, List<DefinitionCoordinates>> getUdChainProvider() {
        return ud::get;
    }

    // for tests
    public Map<Definition, List<UseCoordinates>> getDu() {
        return du;
    }

    // fot tests
    public Map<Use, List<DefinitionCoordinates>> getUd() {
        return ud;
    }

    // ------------- DU -------------

    public record Definition(String varName, int blockId, int instructionIndex) {}

    public static record UseCoordinates(int blockId, int instructionIndex) {}


    // ------------- UD -------------

    public static record Use(String varName, int blockId, int instructionIndex) {}

    public static record DefinitionCoordinates(int blockId, int instructionIndex) {}
}
