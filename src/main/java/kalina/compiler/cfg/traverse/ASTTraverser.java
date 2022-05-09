package kalina.compiler.cfg.traverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.bb.RootBasicBlock;
import kalina.compiler.cfg.converter.ASTExpressionConverter;
import kalina.compiler.cfg.converter.ASTInitExpressionConverter;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.data.TypeDictionary;
import kalina.compiler.cfg.data.TypeDictionaryImpl;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.instructions.DefaultConstructorInstruction;
import kalina.compiler.syntax.parser.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.LocalVariableTableFactory;
import kalina.compiler.syntax.parser2.data.OxmaFunctionTable;

/**
 * @author vlad333rrty
 */
public class ASTTraverser {
    public RootBasicBlock traverse(ASTRootNode root) throws CFGConversionException, IncompatibleTypesException {
        TypeDictionary typeDictionary = new TypeDictionaryImpl();
        fillTypeDictionary(root, typeDictionary);
        ILocalVariableTableFactory localVariableTableFactory = new LocalVariableTableFactory();
        FunctionTableProvider functionTableProvider = getFunctionTableProvider(root);
        ASTExpressionConverter expressionConverter = new ASTExpressionConverter(functionTableProvider);
        ASTInitExpressionConverter initExpressionConverter = new ASTInitExpressionConverter(expressionConverter);
        ClassTraverser classTraverser = new ClassTraverser(
                localVariableTableFactory,
                new TypeChecker(typeDictionary),
                expressionConverter,
                initExpressionConverter
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

    private FunctionTableProvider getFunctionTableProvider(ASTRootNode root) {
        Map<String, OxmaFunctionTable> classNameToFunctionTable = root.getClassNodes().stream()
                .collect(Collectors.toMap(
                        ASTClassNode::getClassName,
                        ASTClassNode::getOxmaFunctionTable));
        return new FunctionTableProvider(classNameToFunctionTable);
    }

    private void fillTypeDictionary(ASTRootNode root, TypeDictionary typeDictionary) {
        root.getClassNodes().stream().map(ASTClassNode::getClassName).forEach(typeDictionary::addType);
    }
}
