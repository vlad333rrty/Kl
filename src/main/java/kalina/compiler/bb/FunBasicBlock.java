package kalina.compiler.bb;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kalina.compiler.codegen.CodeGenUtils;
import kalina.compiler.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FunBasicBlock extends AbstractBasicBlock {
    private final String name;
    private final List<TypeAndName> arguments;
    private final Optional<Type> returnType;
    private final boolean isStatic;

    public FunBasicBlock(String name, List<TypeAndName> arguments, Optional<Type> returnType, boolean isStatic) {
        this.name = name;
        this.arguments = arguments;
        this.returnType = returnType;
        this.isStatic = isStatic;
    }

    public MethodVisitor getMethodVisitor(ClassWriter cw) {
        String descriptor = CodeGenUtils
                .buildDescriptor(arguments.stream().map(TypeAndName::getType).collect(Collectors.toList()), returnType);
        int access = isStatic ? Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC : Opcodes.ACC_PUBLIC; // all functions are public
        MethodVisitor mv = cw.visitMethod(access, name, descriptor, null, null);
        int i = isStatic ? 0 : 1;
        mv.visitCode();
        for (TypeAndName typeAndName : arguments) {
            mv.visitLocalVariable(typeAndName.getName(), typeAndName.getType().getDescriptor(), null, new Label(), new Label(), i++);
        }
        return mv;
    }

    @Override
    public Instruction getInstruction() {
        return null;
    }
}
