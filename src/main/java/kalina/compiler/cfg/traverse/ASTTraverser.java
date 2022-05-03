package kalina.compiler.cfg.traverse;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.bb.RootBasicBlock;
import kalina.compiler.cfg.converter.ASTExpressionConverter;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.instructions.DefaultConstructorInstruction;
import kalina.compiler.syntax.parser.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.ITypeDictionary;
import kalina.compiler.syntax.parser.data.LocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.TypeDictionary;

/**
 * @author vlad333rrty
 */
public class ASTTraverser {
    public RootBasicBlock traverse(ASTRootNode root) throws CFGConversionException {
        ITypeDictionary typeDictionary = new TypeDictionary();
        fillTypeDictionary(root, typeDictionary);
        ILocalVariableTableFactory localVariableTableFactory = new LocalVariableTableFactory();
        ClassTraverser classTraverser = new ClassTraverser(
                localVariableTableFactory,
                new TypeChecker(typeDictionary),
                new ASTExpressionConverter()
        );

        List<ClassBasicBlock> classBasicBlocks = new ArrayList<>();
        for (ASTClassNode classNode : root.getClassNodes()) {
            ClassBasicBlock classBasicBlock = new ClassBasicBlock(new DefaultConstructorInstruction(classNode.getClassName()));
            List<AbstractBasicBlock> bbs = classTraverser.traverse(classNode);
            bbs.forEach(classBasicBlock::addAtTheEnd);
            classBasicBlocks.add(classBasicBlock);
        }

        return new RootBasicBlock(classBasicBlocks);
    }

    private void fillTypeDictionary(ASTRootNode root, ITypeDictionary typeDictionary) {
        root.getClassNodes().stream().map(ASTClassNode::getClassName).forEach(typeDictionary::addType);
    }
}
