package kalina.compiler.odk;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import kalina.compiler.instructions.PrintlnInstruction;

/**
 * @author vlad333rrty
 */
public final class ODKMapper {
    private static final Map<String, ClassAndSignature> nameToInstructionAndSignature = Map.of(
            "println", new ClassAndSignature(PrintlnInstruction.class, new Class[]{List.class})
    );

    public static Optional<ClassAndSignature> getO(String name) {
        return Optional.ofNullable(nameToInstructionAndSignature.get(name));
    }
}
