package kalina.compiler.syntax.parser2.data;

import java.util.List;

import kalina.compiler.bb.TypeAndName;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public record OxmaFunctionInfo(
        List<TypeAndName> arguments,
        Type returnType,
        String ownerClassName,
        boolean isClosure,
        boolean isStatic)
{
}
