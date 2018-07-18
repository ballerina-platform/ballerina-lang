package org.ballerinalang.bir.model;

import java.util.List;

/**
 * Data model for a function.
 * Must have at lease one entry basic block.
 */
public class IrPackage extends IRElement {
    public String pakageId;
    public List<Function> functions;

    public IrPackage(String pakageId, List<Function> functions) {
        this.pakageId = pakageId;
        this.functions = functions;
    }
}

