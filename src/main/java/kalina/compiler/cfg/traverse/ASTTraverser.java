package kalina.compiler.cfg.traverse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.ClassBasicBlock;
import kalina.compiler.bb.RootBasicBlock;
import kalina.compiler.cfg.converter.ASTExpressionConverter;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.data.TypeDictionary;
import kalina.compiler.cfg.data.TypeDictionaryImpl;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.instructions.DefaultConstructorInstruction;
import kalina.compiler.syntax.parser2.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser2.data.LocalVariableTableFactory;

/**
 * @author vlad333rrty
 */
public class ASTTraverser {
    public RootBasicBlock traverse(ASTRootNode root) throws CFGConversionException, IncompatibleTypesException {
        TypeDictionary typeDictionary = new TypeDictionaryImpl();
        fillTypeDictionary(root, typeDictionary);
        ILocalVariableTableFactory localVariableTableFactory = new LocalVariableTableFactory();
        GetFunctionInfoProvider getFunctionInfoProvider = createGetFunctionInfoProvider(root);
        ASTExpressionConverter expressionConverter = new ASTExpressionConverter(getFunctionInfoProvider);
        ClassTraverser classTraverser = new ClassTraverser(
                localVariableTableFactory,
                new TypeChecker(typeDictionary),
                expressionConverter,
                getFunctionInfoProvider
        );

        List<ClassBasicBlock> classBasicBlocks = new ArrayList<>();
        for (ASTClassNode classNode : root.getClassNodes()) {
            ClassBasicBlock classBasicBlock = new ClassBasicBlock(new DefaultConstructorInstruction(classNode.getClassName(), classNode.getParentClassName()));
            List<AbstractBasicBlock> bbs = classTraverser.traverse(classNode);
            bbs.forEach(classBasicBlock::addAtTheEnd);
            classBasicBlocks.add(classBasicBlock);
        }

        return new RootBasicBlock(classBasicBlocks);
    }

    private Map<String, OxmaFunctionInfoProvider> getClassNameToFunctionInfoProvider(ASTRootNode root) {
        return root.getClassNodes().stream()
                .collect(Collectors.toMap(ASTClassNode::getClassName, node -> new OxmaFunctionInfoProvider(node.getOxmaFunctionTable())));
    }

    private GetFunctionInfoProvider createGetFunctionInfoProvider(ASTRootNode root) {
        Map<String, OxmaFunctionInfoProvider> classNameToFunctionInfoProvider = root.getClassNodes().stream()
                .collect(Collectors.toMap(
                        ASTClassNode::getClassName,
                        node -> new OxmaFunctionInfoProvider(node.getOxmaFunctionTable()))
                );
        Map<String, OxmaFunctionInfoProvider> result = new HashMap<>();
        for (ASTClassNode node : root.getClassNodes()) {
            OxmaFunctionInfoProvider nodeFunctionInfoProvider = classNameToFunctionInfoProvider.get(node.getClassName());
            OxmaFunctionInfoProvider parentFunctionInfoProvider = classNameToFunctionInfoProvider.get(node.getParentClassName());
            result.put(node.getClassName(), nodeFunctionInfoProvider.withParent(parentFunctionInfoProvider));
        }
        return new GetFunctionInfoProvider(result);
    }

    private void fillTypeDictionary(ASTRootNode root, TypeDictionary typeDictionary) {
        root.getClassNodes().stream().map(ASTClassNode::getClassName).forEach(typeDictionary::addType);
    }
}
