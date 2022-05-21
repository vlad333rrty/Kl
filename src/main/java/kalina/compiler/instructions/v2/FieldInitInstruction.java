package kalina.compiler.instructions.v2;

import java.util.List;
import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.instructions.Instruction;
import kalina.compiler.instructions.TranslationUtils;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FieldInitInstruction extends Instruction {
    private final String name;
    private final Type type;
    private final ClassEntryUtils.AccessModifier accessModifier;
    private final List<ClassEntryUtils.Modifier> modifiers;

    public FieldInitInstruction(
            String name,
            Type type,
            ClassEntryUtils.AccessModifier accessModifier,
            List<ClassEntryUtils.Modifier> modifiers)
    {
        this.name = name;
        this.type = type;
        this.accessModifier = accessModifier;
        this.modifiers = modifiers;
    }

    @Override
    public void translateToBytecode(Optional<MethodVisitor> mv, Optional<ClassWriter> cw) throws CodeGenException {
        if (cw.isPresent()) {
            ClassWriter classWriter = cw.get();
            classWriter.visitField(TranslationUtils.getModifiers(accessModifier, modifiers), name, type.getDescriptor(), null, null);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
