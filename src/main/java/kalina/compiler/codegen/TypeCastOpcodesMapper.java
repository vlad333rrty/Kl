package kalina.compiler.codegen;

import java.util.Map;
import java.util.Optional;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class TypeCastOpcodesMapper {
    private static final Map<Integer, Map<Integer, Integer>> opcodes = Map.of(
            Type.SHORT, getS2Type(),
            Type.INT, getI2Type(),
            Type.LONG, getL2Type(),
            Type.FLOAT, getF2Type(),
            Type.DOUBLE, getD2Type()
    );

    public static Optional<Integer> getCastOpcode(Type from, Type to) {
        return Optional.ofNullable(opcodes.get(from.getSort())).map(x -> x.get(to.getSort()));
    }

    public static boolean canCast(Type from, Type to) {
        return getCastOpcode(from, to).isPresent();
    }

    private static Map<Integer, Integer> getS2Type() {
        return Map.of(
                Type.LONG, Opcodes.I2L,
                Type.FLOAT, Opcodes.I2F,
                Type.DOUBLE, Opcodes.I2D
        );
    }

    private static Map<Integer, Integer> getI2Type() {
        return Map.of(
                Type.SHORT, Opcodes.I2S,
                Type.LONG, Opcodes.I2L,
                Type.FLOAT, Opcodes.I2F,
                Type.DOUBLE, Opcodes.I2D
        );
    }

    private static Map<Integer, Integer> getL2Type() {
        return Map.of(
                Type.INT, Opcodes.L2I,
                Type.FLOAT, Opcodes.L2F,
                Type.DOUBLE, Opcodes.L2D
        );
    }

    private static Map<Integer, Integer> getF2Type() {
        return Map.of(
                Type.INT, Opcodes.F2I,
                Type.LONG, Opcodes.F2L,
                Type.DOUBLE, Opcodes.F2D
        );
    }

    private static Map<Integer, Integer> getD2Type() {
        return Map.of(
                Type.INT, Opcodes.D2I,
                Type.FLOAT, Opcodes.D2F,
                Type.LONG, Opcodes.D2L
        );
    }
}
