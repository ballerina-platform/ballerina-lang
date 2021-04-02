package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;

/**
 * Represents a coverage method with complexity coverage set to 0.
 *
 * @since 2.0.0
 */
public class NormalizedCoverageMethod implements IMethodCoverage {

    private final IMethodCoverage oldMethodCoverage;

    public NormalizedCoverageMethod(IMethodCoverage oldMethodCoverage) {
        this.oldMethodCoverage = oldMethodCoverage;
    }

    @Override
    public String getDesc() {
        return this.oldMethodCoverage.getDesc();
    }

    @Override
    public String getSignature() {
        return this.oldMethodCoverage.getSignature();
    }

    @Override
    public int getFirstLine() {
        return this.oldMethodCoverage.getFirstLine();
    }

    @Override
    public int getLastLine() {
        return this.oldMethodCoverage.getLastLine();
    }

    @Override
    public ILine getLine(int i) {
        return this.oldMethodCoverage.getLine(i);
    }

    @Override
    public ElementType getElementType() {
        return this.oldMethodCoverage.getElementType();
    }

    @Override
    public String getName() {
        return this.oldMethodCoverage.getName();
    }

    @Override
    public ICounter getInstructionCounter() {
        return this.oldMethodCoverage.getInstructionCounter();
    }

    @Override
    public ICounter getBranchCounter() {
        return this.oldMethodCoverage.getBranchCounter();
    }

    @Override
    public ICounter getLineCounter() {
        return this.oldMethodCoverage.getLineCounter();
    }

    @Override
    public ICounter getComplexityCounter() {
        // Sets the complexity value as 0 for ballerina source method coverages.
        return new PartialCoverageModifiedCounter(null);
    }

    @Override
    public ICounter getMethodCounter() {
        return this.oldMethodCoverage.getMethodCounter();
    }

    @Override
    public ICounter getClassCounter() {
        return this.oldMethodCoverage.getClassCounter();
    }

    @Override
    public ICounter getCounter(CounterEntity counterEntity) {
        if (counterEntity.equals(CounterEntity.COMPLEXITY)) {
            // Sets the complexity value as 0 for ballerina method coverages.
            return new PartialCoverageModifiedCounter(null);
        }
        return oldMethodCoverage.getCounter(counterEntity);
    }

    @Override
    public boolean containsCode() {
        return oldMethodCoverage.containsCode();
    }

    @Override
    public ICoverageNode getPlainCopy() {
        return oldMethodCoverage.getPlainCopy();
    }
}
