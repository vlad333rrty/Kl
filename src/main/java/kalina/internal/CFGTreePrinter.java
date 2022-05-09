package kalina.internal;

import kalina.compiler.bb.AbstractBasicBlock;
import kalina.compiler.bb.ClassBasicBlock;

/**
 * @author vlad333rrty
 */
public final class CFGTreePrinter {
    public static void print(ClassBasicBlock classBasicBlock) {
        print(classBasicBlock, "");
    }

    private static void print(AbstractBasicBlock bb, String margin) {
        System.out.println(String.format("%s%s", margin, bb));
        if (bb.getNext().isPresent()) {
            print(bb.getNext().get(), margin + "\t");
        }
    }
}
