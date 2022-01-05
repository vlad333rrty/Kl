package kalina.compiler.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.BasicBlock;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.bb.FunBasicBlock;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author vlad333rrty
 */
public class CodeGenerationManager {
    public List<CodeGenerationResult> generateByteCode(ClassBasicBlock root) throws CodeGenException {
        List<CodeGenerationResult> result = new ArrayList<>();
        generateByteCodeInt(root, result);
        return result;
    }

    private void generateByteCodeInt(ClassBasicBlock root, List<CodeGenerationResult> result) throws CodeGenException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        root.getInstruction().translateToBytecode(Optional.empty(), Optional.of(cw));
        Optional<AbstractBasicBlock> bbO = root.getNext();
        Optional<MethodVisitor> mv = Optional.empty();
        AbstractBasicBlock bb;
        while (bbO.isPresent()) {
            bb = bbO.get();
            if (bb instanceof ClassBasicBlock) {
                cw.visitEnd();
                result.add(new CodeGenerationResult(cw.toByteArray(), root.getClassName()));
                generateByteCodeInt((ClassBasicBlock) bb, result);
                return;
            }
            if (bb instanceof FunBasicBlock) {
                FunBasicBlock funBasicBlock = (FunBasicBlock) bb;
                mv = Optional.of(funBasicBlock.getMethodVisitor(cw));
            }
            if (bb instanceof BasicBlock) {
                BasicBlock basicBlock = (BasicBlock) bb;
                basicBlock.getInstruction().translateToBytecode(mv, Optional.of(cw));
            }
            bbO = bb.getNext();
        }
        cw.visitEnd();

        result.add(new CodeGenerationResult(cw.toByteArray(), root.getClassName()));
    }
}
