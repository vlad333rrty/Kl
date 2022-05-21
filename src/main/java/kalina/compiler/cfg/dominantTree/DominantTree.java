package kalina.compiler.cfg.dominantTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.builder.nodes.CFGNodeWithBranch;

public class DominantTree {
    private final Vertex root;
    private final List<Vertex> vertices = new ArrayList<>();
    private final Map<Integer, Vertex> idToVertex = new HashMap<>();
    private final Map<Integer, CFGNodeWithBranch> bbToNode = new HashMap<>();

    private int time = 1;

    public DominantTree(List<CFGNodeWithBranch> nodes, CFGNodeWithBranch root) {
        for (CFGNodeWithBranch n : nodes) {
            Vertex v = new Vertex(n.getBasicBlock());
            vertices.add(v);
            idToVertex.put(n.getBasicBlock().getId(), v);
            bbToNode.put(n.getBasicBlock().getId(), n);
        }

        for (CFGNodeWithBranch n : nodes) {
            Vertex v = idToVertex.get(n.getBasicBlock().getId());
            for (AbstractCFGNode child : n.getChildren()) {
                v.getChildren().add(idToVertex.get(child.getBasicBlock().getId()));
                idToVertex.get(child.getBasicBlock().getId()).getAncestors().add(v);
            }
        }

        this.root = idToVertex.get(root.getBasicBlock().getId());
        findDominators();
    }

    public Map<Integer, Set<CFGNodeWithBranch>> getDominanceFrontier() {
        Map<Integer, Set<CFGNodeWithBranch>> dominanceFrontier = new HashMap<>();
        for (Vertex v : vertices) {
            dominanceFrontier.put(v.getBasicBlock().getId(), new HashSet<>());
        }

        for (Vertex v : vertices) {
            if (v.getAncestors().size() > 1) {
                for (Vertex u : v.getAncestors()) {
                    Vertex runner = u;
                    while (runner != getIDom(v)) {
                        dominanceFrontier.get(runner.getBasicBlock().getId()).add(bbToNode.get(v.getBasicBlock().getId()));
                        runner = getIDom(runner);
                    }
                }
            }
        }

        return dominanceFrontier;
    }

    private void findSemiDominators() {
        for (Vertex v : vertices) {
            v.setsDom(v);
            v.setLabel(v);
        }
        PriorityQueue<Vertex> prq = new PriorityQueue<>(vertices);
        for (Vertex w : prq) {
            for (Vertex v : w.getAncestors()) {
                Vertex u = findMin(v);
                if (u.getsDom().compareTo(w.getsDom()) > 0) {
                    w.setsDom(u.getsDom());
                }
            }
            w.setAncestor(w.getParent());
        }
    }

    private Vertex getIDom(Vertex v) {
        return idToVertex.get(v.getBasicBlock().getId()).getDom();
    }

    private void findDominators() {
        traverse();
        findSemiDominators();

        PriorityQueue<Vertex> queue = new PriorityQueue<>(vertices);
        Stack<Vertex> stack = new Stack<>();
        while (!queue.isEmpty()) {
            Vertex w = queue.poll();
            if (w == root) continue;
            stack.push(w);
            for (Vertex v : w.getAncestors()) {
                Vertex u = findMin(v);
                if (u.getsDom().compareTo(w.getsDom()) > 0) w.setsDom(u.getsDom());
            }
            w.setAncestor(w.getParent());
            w.getsDom().getBucket().add(w);
            for (Vertex v : w.getParent().getBucket()) {
                Vertex u = findMin(v);
                v.setDom(u.getsDom() == v.getsDom() ? v.getsDom() : u);
            }
            w.getBucket().clear();
        }

        while (!stack.empty()) {
            Vertex w = stack.pop();
            if (w == root) continue;
            if (w.getDom() != w.getsDom()) w.setDom(w.getDom().getDom());
        }
        root.setDom(null);
    }

    private void traverse() {
        visit(root);
        root.setParent(null);
    }

    private void visit(Vertex v) {
        v.setEntranceTime(time++);
        for (Vertex child : v.getChildren()) {
            if (child.getEntranceTime() == 0) {
                child.setParent(v);
                visit(child);
            }
        }
    }

    private Vertex findMin(Vertex v) {
        if (v.getAncestor() == null) {
            return v;
        }
        Stack<Vertex> stack = new Stack<>();
        Vertex u = v;
        while (u.getAncestor().getAncestor() != null) {
            stack.push(u);
            u = u.getAncestor();
        }
        while (!stack.empty()) {
            v = stack.pop();
            if (v.getAncestor().getLabel().getsDom().compareTo(v.getLabel().getsDom()) > 0) {
                v.setLabel(v.getAncestor().getLabel());
            }
            v.setAncestor(u.getAncestor());
        }
        return v.getLabel();
    }
}
