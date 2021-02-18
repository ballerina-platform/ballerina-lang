package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.ICounter;

/**
 * Represents a counter that eliminates partially covered counter status and coverts it to fully covered status.
 *
 */
public class PartialCoverageModifiedCounter implements ICounter {

    private int covered;
    private int missed;

    public PartialCoverageModifiedCounter(ICounter prevCounter) {
        this.covered = prevCounter.getCoveredCount();
        this.missed = prevCounter.getMissedCount();
        modifyCoverageNumbers();
    }

    /**
     * Modify the covered and missed numbers in cases the counter status is calculated as PARTLY_COVERED.
     * It converts the counter status to FULLY_COVERED.
     */
    public void modifyCoverageNumbers() {
        if (getStatus() == PARTLY_COVERED) {
            covered = covered + missed;
            missed = 0;
        }
    }

    @Override
    public double getValue(CounterValue value) {
        switch (value) {
            case TOTALCOUNT:
                return getTotalCount();
            case MISSEDCOUNT:
                return getMissedCount();
            case COVEREDCOUNT:
                return getCoveredCount();
            case MISSEDRATIO:
                return getMissedRatio();
            case COVEREDRATIO:
                return getCoveredRatio();
            default:
                throw new AssertionError(value);
        }
    }

    @Override
    public int getTotalCount() {
        return covered + missed;
    }

    @Override
    public int getCoveredCount() {
        return covered;
    }

    @Override
    public int getMissedCount() {
        return missed;
    }

    @Override
    public double getCoveredRatio() {
        return (double) covered / (missed + covered);
    }

    @Override
    public double getMissedRatio() {
        return (double) missed / (missed + covered);
    }

    @Override
    public int getStatus() {
        int status = covered > 0 ? FULLY_COVERED : EMPTY;
        if (missed > 0) {
            status |= NOT_COVERED;
        }
        return status;
    }
}
