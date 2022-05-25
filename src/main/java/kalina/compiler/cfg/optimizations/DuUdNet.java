package kalina.compiler.cfg.optimizations;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author vlad333rrty
 */
public class DuUdNet {
    private final Map<Definition, List<InstructionCoordinates>> du;
    private final Map<Use, List<InstructionCoordinates>> ud;

    public DuUdNet(Map<Definition, List<InstructionCoordinates>> du, Map<Use, List<InstructionCoordinates>> ud) {
        this.du = du;
        this.ud = ud;
    }

    public Function<Definition, List<InstructionCoordinates>> getDuChainProvider() {
        return du::get;
    }

    public Function<Use, List<InstructionCoordinates>> getUdChainProvider() {
        return ud::get;
    }

    // for tests
    public Map<Definition, List<InstructionCoordinates>> getDu() {
        return du;
    }

    // for tests
    public Map<Use, List<InstructionCoordinates>> getUd() {
        return ud;
    }

    // ------------- DU -------------

    public record Definition(String varName, int blockId, int instructionIndex) {}

    public static record InstructionCoordinates(int blockId, int instructionIndex) {}


    // ------------- UD -------------

    public static record Use(String varName, int blockId, int instructionIndex) {}
}
