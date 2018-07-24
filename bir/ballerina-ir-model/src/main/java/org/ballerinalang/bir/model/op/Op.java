package org.ballerinalang.bir.model.op;

import org.ballerinalang.bir.model.IrElement;
import org.ballerinalang.bir.model.OpVisitor;

/**
 * Abstract super class of all the op code elements.
 */
public abstract class Op extends IrElement {
    public abstract void accept(OpVisitor visitor);
}
