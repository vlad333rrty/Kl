package kalina.compiler.bb.v2;

import kalina.compiler.instructions.v2.FieldInitInstruction;

/**
 * @author vlad333rrty
 */
public class FieldBasicBlock {
    private final FieldInitInstruction fieldInitInstruction;

    public FieldBasicBlock(FieldInitInstruction fieldInitInstruction) {
        this.fieldInitInstruction = fieldInitInstruction;
    }

    public FieldInitInstruction getFieldInitInstruction() {
        return fieldInitInstruction;
    }
}
