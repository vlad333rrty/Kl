package kalina.compiler.cfg.dominantTree;

import java.util.ArrayList;
import java.util.List;

import kalina.compiler.cfg.bb.BasicBlock;

class Vertex implements Comparable<Vertex> {
    private int entranceTime;
    private Vertex dom, sDom, label, ancestor, parent;
    private final List<Vertex> bucket = new ArrayList<>();
    private final List<Vertex> ancestors = new ArrayList<>();
    private final List<Vertex> children = new ArrayList<>();
    private final BasicBlock basicBlock;

    public Vertex(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public Vertex getDom() {
        return dom;
    }

    public Vertex getsDom() {
        return sDom;
    }

    public Vertex getLabel() {
        return label;
    }

    public Vertex getAncestor() {
        return ancestor;
    }

    public Vertex getParent() {
        return parent;
    }

    public List<Vertex> getBucket() {
        return bucket;
    }

    public List<Vertex> getAncestors() {
        return ancestors;
    }

    public List<Vertex> getChildren() {
        return children;
    }

    public int getEntranceTime() {
        return entranceTime;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public void setDom(Vertex dom) {
        this.dom = dom;
    }

    public void setsDom(Vertex sDom) {
        this.sDom = sDom;
    }

    public void setLabel(Vertex label) {
        this.label = label;
    }

    public void setAncestor(Vertex ancestor) {
        this.ancestor = ancestor;
    }

    public void setParent(Vertex parent) {
        this.parent = parent;
    }

    public void setEntranceTime(int entranceTime) {
        this.entranceTime = entranceTime;
    }

    @Override
    public int compareTo(Vertex o) {
        return Integer.compare(o.entranceTime, entranceTime);
    }
}
