package kalina.compiler.instructions;

import java.util.List;
import java.util.Optional;

import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author vlad333rrty
 */
public final class TranslationUtils {
    public static void translateBlock(Optional<AbstractBasicBlock> bb, Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        Optional<AbstractBasicBlock> current = bb;
        while (current.isPresent()) {
            current.get().getInstruction().translateToBytecode(mv, cw);
            current = current.get().getNext();
        }
    }

    public static int getModifiers(ClassEntryUtils.AccessModifier accessModifier, List<ClassEntryUtils.Modifier> modifiers) {
        return getAccessModifierOpcode(accessModifier)
                | modifiers.stream().map(TranslationUtils::getModifierOpcode).reduce((x, y) -> x | y).orElse(0);
    }

    public static int getAccessModifierOpcode(ClassEntryUtils.AccessModifier accessModifier) {
        return switch (accessModifier) {
            case PRIVATE -> Opcodes.ACC_PRIVATE;
            case PROTECTED -> Opcodes.ACC_PROTECTED;
            case PUBLIC -> Opcodes.ACC_PUBLIC;
        };
    }

    public static int getModifierOpcode(ClassEntryUtils.Modifier modifier) {
        return switch (modifier) {
            case STATIC -> Opcodes.ACC_STATIC;
            case FINAL -> Opcodes.ACC_FINAL;
            case CONST -> Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
        };
    }
}
