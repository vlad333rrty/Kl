package kalina.internal;

import java.io.File;
import java.io.IOException;

import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.ast.expression.ASTExpression;

/**
 * @author vlad333rrty
 */
public final class DotGraphConstructor {
    private static int counter = 0;

    public static void plotGraph(ASTRootNode rootNode) {
        StringBuilder builder = new StringBuilder("graph {\n");
        int rootNum = 0;
        builder.append(counter++).append(" [label=\"ROOT\"]\n");
        for (ASTClassNode classNode : rootNode.getClassNodes()) {
            int classNum = counter;
            addNodeWithLabel(counter++, classNode.getClassName(), builder);
            addEdge(rootNum, classNum, builder);
            for (ASTMethodNode methodNode : classNode.getMethodNodes()) {
                traverseMethodNode(methodNode, builder, classNum);
            }
        }
        builder.append("\n}");

        System.out.println(builder);
        try {
            executeBash(builder.toString());
        } catch (IOException e) {
            System.err.println("ERROR");
        }
    }

    private static void executeBash(String dotCode) throws IOException {
        Runtime.getRuntime().exec(
                "dot -Tpng -o dot.png\n"+dotCode,
                null,
                new File("/home/vlad333rrty/IdeaProjects/KalinaLang"));
    }

    private static void traverseMethodNode(ASTMethodNode node, StringBuilder builder, int parentClassNum) {
        addNodeWithLabel(counter, node.toString(), builder);
        addEdge(parentClassNum, counter, builder);
        counter++;
        StringBuilder stringBuilder = new StringBuilder();
        for (ASTExpression expression : node.getExpressions()) {
            stringBuilder.append(expression).append("\n");
        }
        addNodeWithLabel(counter, stringBuilder.toString(), builder);
        counter++;
    }

    private static void addEdge(int parentNum, int childNum, StringBuilder builder) {
        builder.append(parentNum).append(" --> ").append(childNum).append("\n");
    }

    private static void addNodeWithLabel(int num, String label, StringBuilder builder) {
        builder.append(num).append(" [label=\"").append(label).append("\"]");
    }
}
