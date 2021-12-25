package kalina.compiler.codegen;

import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class TypeCastOpcodesMapper {
    private static final Map<Integer, Map<Integer, Integer>> opcodes = Map.of(
            Type.INT, getI2Type(),
            Type.LONG, getL2Type(),
            Type.FLOAT, getF2Type(),
            Type.DOUBLE, getD2Type()
    );

    public static int getCastOpcode(Type from, Type to) {
        return opcodes.get(from.getSort()).get(to.getSort());
    }

    private static Map<Integer, Integer> getI2Type() {
        return Map.of(
                Type.BYTE, Opcodes.I2B,
                Type.CHAR, Opcodes.I2C,
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
