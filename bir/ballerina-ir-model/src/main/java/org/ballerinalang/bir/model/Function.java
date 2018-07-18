package org.ballerinalang.bir.model;

import java.util.List;

/**
 * Data model for a ir  of a function.
 * Must have at lease one entry basic block.
 */
public class Function {
    public String name;
    public List<BasicBlock> bbs;
}
