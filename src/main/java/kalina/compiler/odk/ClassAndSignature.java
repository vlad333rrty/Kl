package kalina.compiler.odk;

import java.lang.reflect.InvocationTargetException;

import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public class ClassAndSignature {
    private final Class<? extends Instruction> clazz;
    private final Class<?>[] signature;

    public ClassAndSignature(Class<? extends Instruction> clazz, Class<?>[] signature) {
        this.clazz = clazz;
        this.signature = signature;
    }

    public Instruction createInstruction(Object ... arguments)
            throws NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException
    {
        return clazz.getConstructor(signature).newInstance(arguments);
    }
}
