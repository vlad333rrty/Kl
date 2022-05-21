package kalina.compiler.ast;

import java.util.List;
import java.util.stream.Collectors;

import kalina.compiler.bb.TypeAndName;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ASTMethodNode extends ASTNodeBase {
    private final String name;
    private final List<TypeAndName> args;
    private final Type returnType;
    private final boolean isStatic;
    private final ClassEntryUtils.AccessModifier accessModifier;
    private final List<ClassEntryUtils.Modifier> modifier;

    public ASTMethodNode(
            String name,
            List<TypeAndName> args,
            Type returnType,
            boolean isStatic,
            ClassEntryUtils.AccessModifier accessModifier,
            List<ClassEntryUtils.Modifier> modifier)
    {
        this.name = name;
        this.args = args;
        this.returnType = returnType;
        this.isStatic = isStatic;
        this.accessModifier = accessModifier;
        this.modifier = modifier;
    }

    public String getName() {
        return name;
    }

    public List<TypeAndName> getArgs() {
        return args;
    }

    public Type getReturnType() {
        return returnType;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public ClassEntryUtils.AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public List<ClassEntryUtils.Modifier> getModifier() {
        return modifier;
    }

    @Override
    public String toString() {
        String staticMod = isStatic ? "static " : "";
        return staticMod + name + "(" + args.stream().map(TypeAndName::toString).collect(Collectors.joining(", ")) +
                ") -> " + returnType;
    }
}
