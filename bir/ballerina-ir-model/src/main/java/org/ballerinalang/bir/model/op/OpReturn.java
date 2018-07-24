package org.ballerinalang.bir.model.op;

import org.ballerinalang.bir.model.OpVisitor;

/**
 * GoTo opcode.
 * <p>
 * Syntax:
 * return;
 */
public class OpReturn extends Op {

    @Override
    public void accept(OpVisitor visitor) {
        visitor.accept(this);
    }
}
