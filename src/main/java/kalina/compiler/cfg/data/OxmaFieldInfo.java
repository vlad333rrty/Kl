package kalina.compiler.cfg.data;

import java.util.List;

import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public record OxmaFieldInfo(
        Type type,
        ClassEntryUtils.AccessModifier accessModifier,
        List<ClassEntryUtils.Modifier> modifiers,
        String ownerClassName,
        String fieldName)
{
    public boolean isFinal() {
        return modifiers.contains(ClassEntryUtils.Modifier.FINAL) || modifiers.contains(ClassEntryUtils.Modifier.CONST);
    }

    public boolean isStatic() {
        return modifiers.contains(ClassEntryUtils.Modifier.STATIC);
    }
}
