package kalina.compiler.bb.v2;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kalina.compiler.bb.TypeAndName;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.codegen.CodeGenUtils;
import kalina.compiler.syntax.parser2.data.ClassEntryUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class FunBasicBlock {
    private final String name;
    private final List<TypeAndName> arguments;
    private final Optional<Type> returnType;
    private final boolean isStatic;
    private final AbstractCFGNode cfgRoot;
    private final ClassEntryUtils.AccessModifier accessModifier;

    public FunBasicBlock(
            String name,
            List<TypeAndName> arguments,
            Optional<Type> returnType,
            boolean isStatic,
            AbstractCFGNode cfgRoot,
            ClassEntryUtils.AccessModifier accessModifier)
    {
        this.name = name;
        this.arguments = arguments;
        this.returnType = returnType;
        this.isStatic = isStatic;
        this.cfgRoot = cfgRoot;
        this.accessModifier = accessModifier;
    }

    public MethodVisitor getMethodVisitor(ClassWriter cw) {
        String descriptor = CodeGenUtils
                .buildDescriptor(arguments.stream().map(TypeAndName::getType).collect(Collectors.toList()), returnType);
        int accessModifierOpcode = getAccessModifierOpcode();
        int access = isStatic ? accessModifierOpcode | Opcodes.ACC_STATIC : accessModifierOpcode;
        MethodVisitor mv = cw.visitMethod(access, name, descriptor, null, null);
        int i = isStatic ? 0 : 1;
        mv.visitCode();
        for (TypeAndName typeAndName : arguments) {
            mv.visitLocalVariable(typeAndName.getName(), typeAndName.getType().getDescriptor(), null, new Label(), new Label(), i++);
        }
        return mv;
    }

    public AbstractCFGNode getCfgRoot() {
        return cfgRoot;
    }

    private int getAccessModifierOpcode() {
        return switch (accessModifier) {
            case PUBLIC -> Opcodes.ACC_PUBLIC;
            case PROTECTED -> Opcodes.ACC_PROTECTED;
            case PRIVATE -> Opcodes.ACC_PRIVATE;
        };
    }

    public String getName() {
        return name;
    }
}
