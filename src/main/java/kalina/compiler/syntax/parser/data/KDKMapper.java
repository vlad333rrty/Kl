package kalina.compiler.syntax.parser.data;

import java.util.Map;

import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.PrintlnInstruction;

/**
 * @author vlad333rrty
 */
public final class KDKMapper {
    private static final Map<String, Class<? extends Instruction>> nameToInstruction = Map.of(
            "println", PrintlnInstruction.class
    );

    public static Class<? extends Instruction> get(String name) {
        return nameToInstruction.get(name);
    }
}
