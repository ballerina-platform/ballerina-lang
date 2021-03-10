package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;

import java.util.Collection;

/**
 * Represents a coverage class containing normalized package and class names to reflect ballerina source root.
 *
 * @since 2.0.0
 */
public class NormalizedCoverageClass implements IClassCoverage {
    private final IClassCoverage oldClassCoverage;
    private String normalizedPackageName;
    private String normalizedClassName;

    public NormalizedCoverageClass(IClassCoverage oldClassCoverage, String normalizedPackageName,
                                   String normalizedClassName) {
        this.oldClassCoverage = oldClassCoverage;
        this.normalizedPackageName = normalizedPackageName;
        this.normalizedClassName = normalizedClassName;
    }

    @Override
    public long getId() {
        return oldClassCoverage.getId();
    }

    @Override
    public boolean isNoMatch() {
        return oldClassCoverage.isNoMatch();
    }

    @Override
    public String getSignature() {
        return oldClassCoverage.getSignature();
    }

    @Override
    public String getSuperName() {
        return oldClassCoverage.getSuperName();
    }

    @Override
    public String[] getInterfaceNames() {
        return oldClassCoverage.getInterfaceNames();
    }

    @Override
    public String getPackageName() {
        if (this.normalizedPackageName != null) {
            return this.normalizedPackageName;
        }
        return oldClassCoverage.getPackageName();
    }

    @Override
    public String getSourceFileName() {
        return oldClassCoverage.getSourceFileName();
    }

    @Override
    public Collection<IMethodCoverage> getMethods() {
        return oldClassCoverage.getMethods();
    }

    @Override
    public int getFirstLine() {
        return oldClassCoverage.getFirstLine();
    }

    @Override
    public int getLastLine() {
        return oldClassCoverage.getLastLine();
    }

    @Override
    public ILine getLine(int i) {
        return oldClassCoverage.getLine(i);
    }

    @Override
    public ElementType getElementType() {
        return oldClassCoverage.getElementType();
    }

    @Override
    public String getName() {
        if (this.normalizedClassName != null) {
            return this.normalizedClassName;
        }
        return oldClassCoverage.getName();
    }

    @Override
    public ICounter getInstructionCounter() {
        return oldClassCoverage.getInstructionCounter();
    }

    @Override
    public ICounter getBranchCounter() {
        return oldClassCoverage.getBranchCounter();
    }

    @Override
    public ICounter getLineCounter() {
        return oldClassCoverage.getLineCounter();
    }

    @Override
    public ICounter getComplexityCounter() {
        return oldClassCoverage.getComplexityCounter();
    }

    @Override
    public ICounter getMethodCounter() {
        return oldClassCoverage.getMethodCounter();
    }

    @Override
    public ICounter getClassCounter() {
        return oldClassCoverage.getClassCounter();
    }

    @Override
    public ICounter getCounter(CounterEntity counterEntity) {
        return oldClassCoverage.getCounter(counterEntity);
    }

    @Override
    public boolean containsCode() {
        return oldClassCoverage.containsCode();
    }

    @Override
    public ICoverageNode getPlainCopy() {
        return oldClassCoverage.getPlainCopy();
    }
}
