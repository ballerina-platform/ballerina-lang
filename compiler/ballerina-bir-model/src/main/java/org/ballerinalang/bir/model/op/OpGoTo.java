package org.ballerinalang.bir.model.op;

import org.ballerinalang.bir.model.IrBasicBlock;
import org.ballerinalang.bir.model.OpVisitor;

/**
 * GoTo opcode.
 * <p>
 * Syntax:
 * goto `basic-block-index`;
 */
public class OpGoTo extends Op {
    public IrBasicBlock destination;

    public OpGoTo(IrBasicBlock destination) {
        this.destination = destination;
    }

    @Override
    public void accept(OpVisitor visitor) {
        visitor.accept(this);
    }
}
