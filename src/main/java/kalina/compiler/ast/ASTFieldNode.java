package kalina.compiler.ast;

import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ASTFieldNode extends ASTNodeBase {
    private final String name;
    private final Type type;
    private final Optional<ASTExpression> rhs;
    private final ClassEntryUtils.AccessModifier accessModifier;
    private final List<ClassEntryUtils.Modifier> modifiers;

    public ASTFieldNode(
            String name,
            Type type,
            Optional<ASTExpression> rhs,
            ClassEntryUtils.AccessModifier accessModifier,
            List<ClassEntryUtils.Modifier> modifiers)
    {
        this.name = name;
        this.type = type;
        this.rhs = rhs;
        this.accessModifier = accessModifier;
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Optional<ASTExpression> getRhs() {
        return rhs;
    }

    public ClassEntryUtils.AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public List<ClassEntryUtils.Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        return type.getClassName() + " " + name + rhs.map(astExpression -> " = " + astExpression).orElse("");
    }
}
