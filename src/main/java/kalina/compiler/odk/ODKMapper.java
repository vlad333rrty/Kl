package kalina.compiler.odk;

import java.util.Map;
import java.util.Optional;

import kalina.compiler.expressions.v2.funCall.AbstractFunCallExpression;
import kalina.compiler.expressions.v2.funCall.PrintExpression;
import kalina.compiler.expressions.v2.funCall.PrintlnExpression;

/**
 * @author vlad333rrty
 */
public final class ODKMapper {
    private static final Map<String, Class<? extends AbstractFunCallExpression>> nameToInstructionAndSignature = Map.of(
            "println", PrintlnExpression.class,
            "print", PrintExpression.class
    );

    public static Optional<Class<? extends AbstractFunCallExpression>> getO(String name) {
        return Optional.ofNullable(nameToInstructionAndSignature.get(name));
    }
}
