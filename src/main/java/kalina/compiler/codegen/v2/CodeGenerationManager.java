package kalina.compiler.codegen.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.bb.v2.ClassBasicBlock;
import kalina.compiler.codegen.CodeGenException;
import kalina.compiler.codegen.CodeGenerationResult;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class CodeGenerationManager {
    private final CFGByteCodeTranslator cfgByteCodeTranslator;

    public CodeGenerationManager() {
        this.cfgByteCodeTranslator = new CFGByteCodeTranslator();
    }

    public List<CodeGenerationResult> generateByteCode(ClassBasicBlock classBasicBlock) throws CodeGenException {
        List<CodeGenerationResult> result = new ArrayList<>();
        generateByteCodeInt(classBasicBlock, result);
        return result;
    }

    private void generateByteCodeInt(ClassBasicBlock classBasicBlock, List<CodeGenerationResult> result) throws CodeGenException {
        ClassWriter cw = new ClassWriter(0);
        classBasicBlock.getInstruction().translateToBytecode(Optional.empty(), Optional.of(cw));

        for (var field : classBasicBlock.getFieldBasicBlocks()) {
            cfgByteCodeTranslator.translateFieldInitsToByteCode(field, cw);
        }
        for (var fun : classBasicBlock.getEntry()) {
            MethodVisitor mv = fun.getMethodVisitor(cw);
            cfgByteCodeTranslator.translateCFGToByteCode(fun.getCfgRoot(), mv);
        }
        cw.visitEnd();
        result.add(new CodeGenerationResult(cw.toByteArray(), classBasicBlock.getClassName()));
    }
}
