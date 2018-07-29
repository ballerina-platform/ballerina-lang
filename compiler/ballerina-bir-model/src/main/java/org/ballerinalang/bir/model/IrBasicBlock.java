package org.ballerinalang.bir.model;

import org.ballerinalang.bir.model.op.Op;

import java.util.List;

/**
 * Data model for basic block in a function.
 */
public class IrBasicBlock extends IrElement {
    public int index;
    public List<Op> ops;

    public IrBasicBlock(int index, List<Op> ops) {
        this.index = index;
        this.ops = ops;
    }
}
