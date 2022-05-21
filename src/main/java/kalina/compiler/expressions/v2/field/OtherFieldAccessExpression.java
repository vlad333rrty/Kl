package kalina.compiler.expressions.v2.field;

import java.util.Optional;

import kalina.compiler.codegen.CodeGenException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class OtherFieldAccessExpression extends FieldAccessExpression {
    private final Optional<Integer> index;

    public OtherFieldAccessExpression(Type type, boolean isStatic, String ownerClassName, String fieldName) {
        super(type, isStatic, ownerClassName, fieldName);
        this.index = Optional.empty();
    }

    public OtherFieldAccessExpression(Type type, boolean isStatic, String ownerClassName, String fieldName, int index) {
        super(type, isStatic, ownerClassName, fieldName);
        this.index = Optional.of(index);
    }

    @Override
    public void translateToBytecode(MethodVisitor mv) throws CodeGenException {
        index.ifPresent(i -> expressionCodeGen.loadVariable(mv, type.getOpcode(Opcodes.ILOAD), i));
        super.translateToBytecode(mv);
    }
}
