package kalina.compiler.cfg.data;

import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;

/**
 * @author vlad333rrty
 */
public record RuntimeConstantPool(
        String className,
        OxmaFunctionTable oxmaFunctionTable)
{
}
