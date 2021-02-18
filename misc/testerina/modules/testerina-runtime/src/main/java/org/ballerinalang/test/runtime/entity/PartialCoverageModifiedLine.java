package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;

public class ModifiedLineImpl implements ILine {

    private final ModifiedCounterImpl instructions;
    private final ModifiedCounterImpl branches;

    public ModifiedLineImpl(ICounter instructions, ICounter branches) {
        this.instructions = new ModifiedCounterImpl(instructions);
        this.branches = new ModifiedCounterImpl(branches);
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
