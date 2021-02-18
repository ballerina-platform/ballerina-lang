package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;

/**
 * Represents a line modified to consider partially covered coverage info as fully covered.
 *
 */
public class PartialCoverageModifiedLine implements ILine {

    private final PartialCoverageModifiedCounter instructions;
    private final PartialCoverageModifiedCounter branches;

    public PartialCoverageModifiedLine(ICounter instructions, ICounter branches) {
        this.instructions = new PartialCoverageModifiedCounter(instructions);
        this.branches = new PartialCoverageModifiedCounter(branches);
    }

    @Override
    public ICounter getInstructionCounter() {
        return instructions;
    }

    @Override
    public ICounter getBranchCounter() {
        return branches;
    }

    @Override
    public int getStatus() {
        return branches.getStatus() | instructions.getStatus();
    }
}
