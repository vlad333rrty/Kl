package kalina.internal;

import java.io.File;
import java.io.IOException;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import kalina.compiler.ast.ASTClassNode;
import kalina.compiler.ast.ASTMethodNode;
import kalina.compiler.ast.ASTRootNode;
import kalina.compiler.ast.expression.ASTExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vlad333rrty
 */
public final class DotGraphConstructor {
    private static final Logger logger = LogManager.getLogger(DotGraphConstructor.class);

    private static int counter = 0;

    public static void plotGraph(ASTRootNode rootNode, String fileName) {
        String graph = buildGraphviz(rootNode);
        try {
            logger.info("Trying to plot graph");
            plot(graph, fileName);
            logger.info("Successfully plotted the graph");
        } catch (IOException e) {
            logger.error("Failed to plot graph: {}", e.getMessage());
        }
    }

    private static void plot(String dotCode, String fileName) throws IOException {
        MutableGraph graph = new Parser().read(dotCode);
        String pathToCompiler = System.getProperty("user.dir");
        Graphviz.fromGraph(graph).render(Format.PNG).toFile(new File(pathToCompiler + "/internal/" + fileName));
    }

    private static String buildGraphviz(ASTRootNode rootNode) {
        StringBuilder builder = new StringBuilder("digraph {\n");
        int rootNum = 0;
        builder.append(counter++).append(" [label=\"ROOT\"]\n");
        for (ASTClassNode classNode : rootNode.getClassNodes()) {
            int classNum = counter;
            addNodeWithLabel(counter++, classNode.toString(), builder);
            addEdge(rootNum, classNum, builder);
            for (ASTMethodNode methodNode : classNode.getMethodNodes()) {
                traverseMethodNode(methodNode, builder, classNum);
            }
        }
        builder.append("\n}");
        return builder.toString();
    }

    private static void traverseMethodNode(ASTMethodNode node, StringBuilder builder, int parentClassNum) {
        addNodeWithLabel(counter, node.toString(), builder);
        addEdge(parentClassNum, counter, builder);
        int methodNum = counter;
        counter++;
        StringBuilder stringBuilder = new StringBuilder();
        for (ASTExpression expression : node.getExpressions()) {
            stringBuilder.append(expression).append("\n");
        }
        addNodeWithLabel(counter, stringBuilder.toString(), builder);
        addEdge(methodNum, counter, builder);
        counter++;
    }

    private static void addEdge(int parentNum, int childNum, StringBuilder builder) {
        builder.append(parentNum).append(" -> ").append(childNum).append("\n");
    }

    private static void addNodeWithLabel(int num, String label, StringBuilder builder) {
        builder.append(num).append(" [label=\"").append(label).append("\"]\n");
    }
}
