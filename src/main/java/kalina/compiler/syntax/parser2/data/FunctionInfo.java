package kalina.compiler.syntax.parser2.data;

import java.util.List;
import java.util.Optional;

import kalina.compiler.bb.TypeAndName;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FunctionInfo {
    private final List<TypeAndName> arguments;
    private final Optional<Type> returnType;
    private final String ownerClass;
    private final boolean isClosure;
    private final boolean isStatic;

    public FunctionInfo(List<TypeAndName> arguments, Optional<Type> returnType, String ownerClass, boolean isClosure, boolean isStatic) {
        this.arguments = arguments;
        this.returnType = returnType;
        this.ownerClass = ownerClass;
        this.isClosure = isClosure;
        this.isStatic = isStatic;
    }

    public List<TypeAndName> getArguments() {
        return arguments;
    }

    public Optional<Type> getReturnType() {
        return returnType;
    }

    public String getOwnerClass() {
        return ownerClass;
    }

    public boolean isClosure() {
        return isClosure;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
