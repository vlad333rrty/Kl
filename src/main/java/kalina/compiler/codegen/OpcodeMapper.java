package kalina.compiler.codegen;

import java.util.Map;

import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;

/**
 * @author vlad333rrty
 */
public class OpcodeMapper {
    public static final Map<String, Integer> storeOpcodes;
    public static final Map<String, Integer> loadOpcodes;

    static {
        storeOpcodes = createStoreOpcodes();
        loadOpcodes = createLoadOpcodes();
    }

    private static Map<String, Integer> createStoreOpcodes() {
        return Map.of(
                "char", ISTORE,
                "short", ISTORE,
                "int", ISTORE,
                "long", LSTORE,
                "float", FSTORE,
                "double", DSTORE,
                "bool", ISTORE
        );
    }

    private static Map<String, Integer> createLoadOpcodes() {
        return Map.of(
                "char", ILOAD,
                "short", ILOAD,
                "int", ILOAD,
                "long", LLOAD,
                "float", FLOAD,
                "double", DLOAD,
                "bool", ILOAD
        );
    }
}
