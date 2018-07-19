package org.ballerinalang.bir.model.op;

import org.ballerinalang.bir.model.IrBasicBlock;

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
}
