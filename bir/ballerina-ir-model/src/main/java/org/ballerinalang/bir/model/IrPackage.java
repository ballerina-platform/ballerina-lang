package org.ballerinalang.bir.model;

import java.util.List;

/**
 * Data model for a function.
 * Must have at lease one entry basic block.
 */
public class IrPackage extends IrElement {
    public String pakageId;
    public List<IrFunction> functions;

    public IrPackage(String pakageId, List<IrFunction> functions) {
        this.pakageId = pakageId;
        this.functions = functions;
    }
}

