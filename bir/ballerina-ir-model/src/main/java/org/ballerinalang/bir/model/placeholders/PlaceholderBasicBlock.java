package org.ballerinalang.bir.model.placeholders;

import org.ballerinalang.bir.model.IrBasicBlock;

import java.util.Collections;

/**
 * Placeholder element representing a basic block.
 */
public class PlaceholderBasicBlock extends IrBasicBlock {

    public PlaceholderBasicBlock(int index) {
        super(index, Collections.emptyList());
    }
}
