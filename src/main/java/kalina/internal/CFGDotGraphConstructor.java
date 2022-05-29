package kalina.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import kalina.compiler.bb.v2.ClassBasicBlock;
import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

/**
 * @author vlad333rrty
 */
public class CFGDotGraphConstructor {
    private static final Logger logger = LogManager.getLogger(CFGDotGraphConstructor.class);

    private static Set<Integer> traversedNodes;

    public static void plotMany(List<ClassBasicBlock> classBasicBlocks) {
        plotMany(classBasicBlocks, "cfg");
    }

    public static void plotMany(List<ClassBasicBlock> classBasicBlocks, String relativePath) {
        int j = 0;
        for (var classBB : classBasicBlocks) {
            int i = 0;
            for (var fun : classBB.getEntry()) {
                plotGraph(fun.getCfgRoot(), relativePath + j + "_" + i++ + ".png");
            }
            j++;
        }
    }

    public static void plotGraph(AbstractCFGNode rootNode, String fileName) {
        traversedNodes = new HashSet<>();
        MutableGraph graph = mutGraph().setDirected(true).graphAttrs().add(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM));
        buildGraph(rootNode, graph);
        try {
            logger.info("Trying to plot graph");
            String pathToCompiler = System.getProperty("user.dir");
            Graphviz.fromGraph(graph).render(Format.PNG).toFile(new File(pathToCompiler + "/internal/" + fileName));
            logger.info("Successfully plotted the graph");
        } catch (IOException e) {
            logger.error("Failed to plot graph: {}", e.getMessage());
        }
    }

    private static void buildGraph(AbstractCFGNode node, MutableGraph graph) {
        MutableNode graphNode = mutNode(String.valueOf(node.getBasicBlock().toString()));
        graph.add(graphNode);
        traversedNodes.add(node.getId());
        buildGraph(node, graph, graphNode);
    }

    private static void buildGraph(AbstractCFGNode node, MutableGraph graph, MutableNode graphNode) {
        List<MutableNode> childNodes = node.getChildren().stream()
                .map(child -> mutNode(child.getBasicBlock().toString()))
                .toList();
        graph.add(childNodes);
        graphNode.addLink(childNodes);
        traversedNodes.add(node.getId());
        for (int i = 0; i < childNodes.size(); i++) {
            if (!traversedNodes.contains(node.getChildren().get(i).getId())) {
                buildGraph(node.getChildren().get(i), graph, childNodes.get(i));
            }
        }
    }
}
