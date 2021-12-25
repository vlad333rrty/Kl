package kalina.compiler.bb;

import java.util.Optional;

import kalina.compiler.instructions.Instruction;

/**
 * @author vlad333rrty
 */
public abstract class AbstractBasicBlock {
    private Optional<AbstractBasicBlock> next;

    public AbstractBasicBlock() {
        this.next = Optional.empty();
    }

    public Optional<AbstractBasicBlock> getNext() {
        return next;
    }

    public void addAtTheEnd(AbstractBasicBlock next) {
        AbstractBasicBlock current = this;
        while (current.next.isPresent()) {
            current = current.next.get();
        }
        current.next = Optional.of(next);
    }

    public abstract Instruction getInstruction();
}
