package kalina.compiler.cfg.traverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTFieldNode;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.BasicBlock;
import kalina.compiler.bb.FunBasicBlock;
import kalina.compiler.cfg.converter.ExpressionConverterFactory;
import kalina.compiler.cfg.data.AbstractLocalVariableTable;
import kalina.compiler.cfg.data.GetFieldInfoProvider;
import kalina.compiler.cfg.data.GetFunctionInfoProvider;
import kalina.compiler.cfg.data.ILocalVariableTableFactory;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.instructions.v2.FieldInitInstruction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public class ClassTraverser {
    private static final Logger logger = LogManager.getLogger(ClassTraverser.class);

    private final ILocalVariableTableFactory localVariableTableFactory;
    private final TypeChecker typeChecker;
    private final GetFunctionInfoProvider getFunctionInfoProvider;
    private final GetFieldInfoProvider getFieldInfoProvider;

    public ClassTraverser(
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

    public List<AbstractBasicBlock> traverse(ASTClassNode classNode) throws CFGConversionException, IncompatibleTypesException {
        List<AbstractBasicBlock> result = new ArrayList<>();
        OxmaFunctionInfoProvider functionInfoProvider = getFunctionInfoProvider.getFunctionTable(classNode.getClassName()).orElseThrow();
        for (ASTFieldNode node : classNode.getFieldNodes()) {
            FieldInitInstruction fieldInitInstruction = new FieldInitInstruction(
                    node.getName(),
                    node.getType(),
                    node.getAccessModifier(),
                    node.getModifiers());
            result.add(new BasicBlock(fieldInitInstruction));
        }
        for (ASTMethodNode node : classNode.getMethodNodes()) {
            FunBasicBlock funBasicBlock =
                    new FunBasicBlock(node.getName(), node.getArgs(), Optional.of(node.getReturnType()), node.isStatic());
            AbstractLocalVariableTable localVariableTable = node.isStatic()
                    ? localVariableTableFactory.createLocalVariableTableForStatic()
                    : localVariableTableFactory.createLocalVariableTableForNonStatic();
            node.getArgs().forEach(arg -> localVariableTable.addVariable(arg.getName(), arg.getType()));
            MethodEntryTraverser methodEntryTraverser = new MethodEntryTraverser(
                    ExpressionConverterFactory.createExpressionConverter(getFunctionInfoProvider, getFieldInfoProvider, node.isStatic()),
                    typeChecker,
                    node.getReturnType(),
                    functionInfoProvider,
                    getFieldInfoProvider.getFieldInfoProvider(classNode.getClassName()).orElseThrow());
            List<AbstractBasicBlock> bbs = methodEntryTraverser.traverse(node.getExpressions(), localVariableTable);
            bbs.forEach(funBasicBlock::addAtTheEnd);
            result.add(funBasicBlock);
        }

        return result;
    }
}
