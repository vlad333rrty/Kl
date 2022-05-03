package kalina.compiler.ast;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ASTFieldNode extends ASTNodeBase {
    private final String name;
    private final Type type;
    private final boolean isStatic;
    private final int accessModifier;

    public ASTFieldNode(String name, Type type, boolean isStatic, int accessModifier) {
        this.name = name;
        this.type = type;
        this.isStatic = isStatic;
        this.accessModifier = accessModifier;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public int getAccessModifier() {
        return accessModifier;
    }
}
