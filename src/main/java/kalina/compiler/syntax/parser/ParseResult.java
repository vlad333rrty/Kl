package kalina.compiler.syntax.parser;

import java.util.Optional;

import kalina.compiler.bb.ClassBasicBlock;

/**
 * @author vlad333rrty
 */
public class ParseResult {
    private final Optional<ClassBasicBlock> root;
    private final ParsingStatus status;

    public ParseResult(Optional<ClassBasicBlock> root, ParsingStatus status) {
        this.root = root;
        this.status = status;
    }

    public ParsingStatus getStatus() {
        return status;
    }

    public Optional<ClassBasicBlock> getRoot() {
        return root;
    }
}
