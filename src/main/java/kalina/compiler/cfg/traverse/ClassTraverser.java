package kalina.compiler.cfg.traverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.ast.expression.ASTAssignInstruction;
import kalina.compiler.ast.expression.ASTExpression;
import kalina.compiler.ast.expression.ASTIfInstruction;
import kalina.compiler.ast.expression.ASTInitInstruction;
import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.BasicBlock;
import kalina.compiler.bb.FunBasicBlock;
import kalina.compiler.cfg.converter.ASTExpressionConverter;
import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.expressions.CondExpression;
import kalina.compiler.expressions.Expression;
import kalina.compiler.expressions.LHS;
import kalina.compiler.expressions.VariableNameAndIndex;
import kalina.compiler.instructions.v2.AssignInstruction;
import kalina.compiler.instructions.v2.InitInstruction;
import kalina.compiler.syntax.parser.data.AbstractLocalVariableTable;
import kalina.compiler.syntax.parser.data.ILocalVariableTableFactory;
import kalina.compiler.syntax.parser.data.TypeAndIndex;
import kalina.compiler.syntax.parser.data.VariableInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class ClassTraverser {
    private static final Logger logger = LogManager.getLogger(ClassTraverser.class);

    private final ILocalVariableTableFactory localVariableTableFactory;
    private final TypeChecker typeChecker;
    private final ASTExpressionConverter astExpressionConverter;

    public ClassTraverser(
            ILocalVariableTableFactory localVariableTableFactory,
            TypeChecker typeChecker,
            ASTExpressionConverter astExpressionConverter)
    {
        this.localVariableTableFactory = localVariableTableFactory;
        this.typeChecker = typeChecker;
        this.astExpressionConverter = astExpressionConverter;
    }

    public List<AbstractBasicBlock> traverse(ASTClassNode classNode) throws CFGConversionException {
        List<AbstractBasicBlock> result = new ArrayList<>();
        for (ASTMethodNode node : classNode.getMethodNodes()) {
            FunBasicBlock funBasicBlock =
                    new FunBasicBlock(node.getName(), node.getArgs(), Optional.of(node.getReturnType()), node.isStatic());
            AbstractLocalVariableTable localVariableTable = node.isStatic()
                    ? localVariableTableFactory.createLocalVariableTableForStatic()
                    : localVariableTableFactory.createLocalVariableTableForNonStatic();
            node.getArgs().forEach(arg -> localVariableTable.addVariable(arg.getName(), arg.getType()));
            for (ASTExpression expression : node.getExpressions()) {
                AbstractBasicBlock bb = convertExpression(expression, classNode, localVariableTable);
                funBasicBlock.addAtTheEnd(bb);
            }
            result.add(funBasicBlock);
        }

        return result;
    }

    public AbstractBasicBlock convertExpression(
            ASTExpression expression,
            ASTClassNode classNode,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException
    {
        final AbstractBasicBlock bb;
        if (expression instanceof ASTInitInstruction initInstruction) {
            bb = constructInitInstruction(initInstruction, classNode, localVariableTable);
        } else if (expression instanceof ASTAssignInstruction assignInstruction) {
            bb = constructAssignInstruction(assignInstruction, classNode, localVariableTable);
        } else {
            throw new UnsupportedOperationException();
        }
        return bb;
    }

    private void constructIfInstruction(
            ASTClassNode classNode,
            AbstractBasicBlock parentBlock,
            AbstractLocalVariableTable localVariableTable,
            ASTIfInstruction ifInstruction)
    {
        CondExpression condExpression = astExpressionConverter
                .convertCondExpression(ifInstruction.condExpression(), localVariableTable, classNode.getOxmaFunctionTable());
        List<Expression> thenBr = ifInstruction.thenBr().getExpressions().stream()
                .map(expr -> astExpressionConverter.convert(expr, localVariableTable, classNode.getOxmaFunctionTable()))
                .toList();
    }

    private AbstractBasicBlock constructAssignInstruction(
            ASTAssignInstruction assignInstruction,
            ASTClassNode classNode,
            AbstractLocalVariableTable localVariableTable)
    {
        List<VariableInfo> variableInfos = assignInstruction.lhs().stream()
                .map(name -> {
                    Optional<TypeAndIndex> typeAndIndexO = localVariableTable.findVariable(name);
                    if (typeAndIndexO.isEmpty()) {
                        logger.error("No info found for variable {}", name);
                        return null;
                    }
                    return new VariableInfo(name, typeAndIndexO.get().getIndex(), typeAndIndexO.get().getType());
                })
                .filter(Objects::nonNull)
                .toList();

        AssignInstruction instruction = new AssignInstruction(
                variableInfos,
                assignInstruction.rhs().stream()
                        .map(expr -> astExpressionConverter.convert(expr, localVariableTable, classNode.getOxmaFunctionTable()))
                        .toList()
        );
        return new BasicBlock(instruction);
    }

    private AbstractBasicBlock constructInitInstruction(
            ASTInitInstruction initInstruction,
            ASTClassNode classNode,
            AbstractLocalVariableTable localVariableTable) throws CFGConversionException
    {
        Type type = initInstruction.lhs().type();
        if (!Assert.assertIsValidDeclarationType(type, typeChecker)) {
            throw new CFGConversionException();
        }
        List<String> filteredVariableNames = initInstruction.lhs()
                .variableNames()
                .stream()
                .filter(name -> Assert.assertMultipleVariableDeclarations(name, localVariableTable))
                .toList();
        List<VariableNameAndIndex> variableNameAndIndices = new ArrayList<>();
        filteredVariableNames.forEach(name -> {
            int index = localVariableTable.addVariable(name, type);
            variableNameAndIndices.add(new VariableNameAndIndex(name, index));
        });
        LHS lhs = new LHS(variableNameAndIndices, type);
        List<Expression> rhs = initInstruction.rhs().stream()
                .map(expr -> astExpressionConverter.convert(expr, localVariableTable, classNode.getOxmaFunctionTable()))
                .toList();
        InitInstruction instruction = new InitInstruction(lhs, rhs);
        return new BasicBlock(instruction);
    }
}
