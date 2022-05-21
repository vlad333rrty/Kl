package kalina.compiler.cfg.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTFieldNode;
import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;
import kalina.compiler.cfg.data.ILocalVariableTableFactory;
import kalina.compiler.cfg.data.LocalVariableTableFactory;
import kalina.compiler.cfg.data.OxmaFieldInfo;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.data.TypeDictionary;
import kalina.compiler.cfg.data.TypeDictionaryImpl;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.traverse.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.validator.IncompatibleTypesException;

/**
 * @author vlad333rrty
 */
public class CFGBuilder {
    public List<AbstractCFGNode> build(ASTRootNode root) throws CFGConversionException, IncompatibleTypesException {
        TypeDictionary typeDictionary = new TypeDictionaryImpl();
        fillTypeDictionary(root, typeDictionary);
        ILocalVariableTableFactory localVariableTableFactory = new LocalVariableTableFactory();
        GetFunctionInfoProvider getFunctionInfoProvider = createGetFunctionInfoProvider(root);
        GetFieldInfoProvider fieldInfoProvider = createFieldInfoProvider(root);
        ClassCFGBuilder classCFGBuilder = new ClassCFGBuilder(
                localVariableTableFactory,
                new TypeChecker(typeDictionary),
                getFunctionInfoProvider,
                fieldInfoProvider
        );

        List<AbstractCFGNode> classBasicBlocks = new ArrayList<>();
        for (ASTClassNode classNode : root.getClassNodes()) {
            List<AbstractCFGNode> bbs = classCFGBuilder.traverse(classNode);
            classBasicBlocks.addAll(bbs);
        }

        return classBasicBlocks;
    }

    private GetFieldInfoProvider createFieldInfoProvider(ASTRootNode root) {
        return new GetFieldInfoProvider(root.getClassNodes().stream().collect(Collectors.toMap(
                ASTClassNode::getClassName,
                x -> getNameToFiledInfo(x.getFieldNodes(), x.getClassName())
        )));
    }

    private Function<String, Optional<OxmaFieldInfo>> getNameToFiledInfo(List<ASTFieldNode> fieldNodes, String ownerName) {
        Map<String, OxmaFieldInfo> infoMap = fieldNodes.stream().collect(Collectors.toMap(
                ASTFieldNode::getName,
                a -> new OxmaFieldInfo(a.getType(), a.getAccessModifier(), a.getModifiers(), ownerName)
        ));
        return name -> Optional.ofNullable(infoMap.get(name));
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
