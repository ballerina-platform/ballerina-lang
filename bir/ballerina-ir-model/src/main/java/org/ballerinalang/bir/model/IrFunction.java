package org.ballerinalang.bir.model;

import java.util.List;

/**
 * Data model for a ir  of a function.
 * Must have at lease one entry basic block.
 */
public class IrFunction {
    public String name;
    public List<IrBasicBlock> bbs;

    public IrFunction(String name, List<IrBasicBlock> bbs) {
        this.name = name;
        this.bbs = bbs;
    }
}
