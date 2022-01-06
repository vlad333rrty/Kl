package kalina.compiler.codegen;

import java.util.List;
import java.util.Optional;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public final class CodeGenUtils {
    public static String buildDescriptor(List<Type> arguments, Optional<Type> returnType) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (Type type : arguments) {
            builder.append(type.getDescriptor());
        }
        builder.append(")");
        if (returnType.isPresent()) {
            builder.append(returnType.get().getDescriptor());
        } else {
            builder.append("V");
        }
        return builder.toString();
    }
}
