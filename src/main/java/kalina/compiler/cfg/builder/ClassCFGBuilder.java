package kalina.compiler.cfg.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.bb.FunBasicBlock;
import kalina.compiler.cfg.builder.items.ASTDoProcessor;
import kalina.compiler.cfg.builder.items.ASTForProcessor;
import kalina.compiler.cfg.builder.items.ASTIfProcessor;
import kalina.compiler.cfg.builder.items.BranchExpressionConverter;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.converter.AbstractExpressionConverter;
import kalina.compiler.cfg.converter.ExpressionConverterFactory;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;
import kalina.compiler.cfg.data.ILocalVariableTableFactory;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.traverse.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class ClassCFGBuilder {
    private static final Logger logger = LogManager.getLogger(ClassCFGBuilder.class);

    private final ILocalVariableTableFactory localVariableTableFactory;
    private final TypeChecker typeChecker;
    private final GetFunctionInfoProvider getFunctionInfoProvider;
    private final GetFieldInfoProvider getFieldInfoProvider;

    public ClassCFGBuilder(
            ILocalVariableTableFactory localVariableTableFactory,
            TypeChecker typeChecker,
            GetFunctionInfoProvider getFunctionInfoProvider,
            GetFieldInfoProvider getFieldInfoProvider)
    {
        this.localVariableTableFactory = localVariableTableFactory;
        this.typeChecker = typeChecker;
        this.getFunctionInfoProvider = getFunctionInfoProvider;
        this.getFieldInfoProvider = getFieldInfoProvider;
    }

    public List<AbstractCFGNode> traverse(ASTClassNode classNode) throws CFGConversionException, IncompatibleTypesException {
        List<AbstractCFGNode> result = new ArrayList<>();
        OxmaFunctionInfoProvider functionInfoProvider = getFunctionInfoProvider.getFunctionTable(classNode.getClassName()).orElseThrow();
//        for (ASTFieldNode node : classNode.getFieldNodes()) {
//            FieldInitInstruction fieldInitInstruction = new FieldInitInstruction(
//                    node.getName(),
//                    node.getType(),
//                    node.getAccessModifier(),
//                    node.getModifiers());
//            result.add(new BasicBlock(fieldInitInstruction));
//        }
        for (ASTMethodNode node : classNode.getMethodNodes()) {
            FunBasicBlock funBasicBlock =
                    new FunBasicBlock(node.getName(), node.getArgs(), Optional.of(node.getReturnType()), node.isStatic());
            AbstractLocalVariableTable localVariableTable = node.isStatic()
                    ? localVariableTableFactory.createLocalVariableTableForStatic()
                    : localVariableTableFactory.createLocalVariableTableForNonStatic();
            node.getArgs().forEach(arg -> localVariableTable.addVariable(arg.getName(), arg.getType()));

            MethodEntryCFGBuilder methodEntryCFGBuilder = getMethodEntryCFGBuilder(node, classNode.getClassName(), functionInfoProvider);
            AbstractCFGNode root = methodEntryCFGBuilder.build(node.getExpressions(), localVariableTable);
            result.add(root);
        }

        return result;
    }

    private MethodEntryCFGBuilder getMethodEntryCFGBuilder(
            ASTMethodNode node,
            String className,
            OxmaFunctionInfoProvider functionInfoProvider)
    {
        AbstractExpressionConverter expressionConverter = ExpressionConverterFactory
                .createExpressionConverter(getFunctionInfoProvider, getFieldInfoProvider, node.isStatic());
        var fieldInfoProvider = getFieldInfoProvider.getFieldInfoProvider(className).orElseThrow();
        InstructionCFGBuilder instructionBuilder = new InstructionCFGBuilder(
                expressionConverter,
                typeChecker,
                node.getReturnType(),
                functionInfoProvider,
                fieldInfoProvider
        );
        BranchExpressionConverter branchExpressionConverter = new BranchExpressionConverter(
                new ASTIfProcessor(expressionConverter, functionInfoProvider, fieldInfoProvider),
                new ASTForProcessor(expressionConverter, functionInfoProvider, fieldInfoProvider, instructionBuilder),
                new ASTDoProcessor(expressionConverter, functionInfoProvider, fieldInfoProvider)
        );
        MethodEntryCFGTraverser traverser = new MethodEntryCFGTraverser(instructionBuilder, branchExpressionConverter);
        return new MethodEntryCFGBuilder(traverser);
    }
}
