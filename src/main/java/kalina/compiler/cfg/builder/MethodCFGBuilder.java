package kalina.compiler.cfg.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.bb.TypeAndName;
import kalina.compiler.bb.v2.FunBasicBlock;
import kalina.compiler.cfg.builder.instruction.AbstractInstructionCFGBuilder;
import kalina.compiler.cfg.builder.instruction.InstructionCFGBuilderFactory;
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
import kalina.compiler.cfg.data.OxmaFunctionInfoProvider;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;

/**
 * @author vlad333rrty
 */
public class MethodCFGBuilder {
    private final ILocalVariableTableFactory localVariableTableFactory;
    private final TypeChecker typeChecker;
    private final GetFunctionInfoProvider getFunctionInfoProvider;
    private final GetFieldInfoProvider getFieldInfoProvider;

    public MethodCFGBuilder(
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

    public List<FunBasicBlock> build(ASTClassNode classNode) throws CFGConversionException, IncompatibleTypesException {
        List<FunBasicBlock> result = new ArrayList<>();
        OxmaFunctionInfoProvider functionInfoProvider = getFunctionInfoProvider.getFunctionTable(classNode.getClassName()).orElseThrow();
        for (ASTMethodNode node : classNode.getMethodNodes()) {
            AbstractLocalVariableTable localVariableTable = node.isStatic()
                    ? localVariableTableFactory.createLocalVariableTableForStatic()
                    : localVariableTableFactory.createLocalVariableTableForNonStatic();
            node.getArgs().forEach(arg -> localVariableTable.addVariable(arg.getName(), arg.getType()));

            MethodEntryCFGBuilder methodEntryCFGBuilder = getMethodEntryCFGBuilder(node, classNode.getClassName(), functionInfoProvider);
            AbstractCFGNode root = methodEntryCFGBuilder.build(
                    node.getExpressions(),
                    localVariableTable,
                    node.getArgs().stream().map(TypeAndName::getName).toList());
            FunBasicBlock funBasicBlock = new FunBasicBlock(
                    node.getName(),
                    node.getArgs(),
                    Optional.of(node.getReturnType()),
                    node.isStatic(),
                    root,
                    node.getAccessModifier());
            result.add(funBasicBlock);
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
        AbstractInstructionCFGBuilder instructionBuilder = InstructionCFGBuilderFactory.createInstructionCFGBuilder(
                node.isStatic(),
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
