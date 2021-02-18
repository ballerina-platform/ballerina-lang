package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;

import java.util.List;

/**
 * Represents a source file containing lines modified to consider partially covered coverage info as fully covered.
 *
 */
public class PartialCoverageModifiedSourceFile implements ISourceFileCoverage {

    private final ISourceFileCoverage oldSourceFile;
    private final List<ILine> modifiedLines;

    public PartialCoverageModifiedSourceFile(ISourceFileCoverage oldSourcefile, List<ILine> modifiedLines) {
        this.oldSourceFile = oldSourcefile;
        this.modifiedLines = modifiedLines;
    }

    @Override
    public int getFirstLine() {
        return oldSourceFile.getFirstLine();
    }

    @Override
    public int getLastLine() {
        return oldSourceFile.getLastLine();
    }

    /**
     * Returns the modified lines instead of lines stored in the original source file.
     */
    @Override
    public ILine getLine(int nr) {
        if (modifiedLines.size() == 0 || nr < getFirstLine() || nr > getLastLine()) {
            return oldSourceFile.getLine(nr);
        }
        ILine reqLine = modifiedLines.get(nr - getFirstLine());
        return reqLine == null ? oldSourceFile.getLine(nr) : reqLine;
    }

    @Override
    public String getPackageName() {
        return oldSourceFile.getPackageName();
    }

    @Override
    public ElementType getElementType() {
        return oldSourceFile.getElementType();
    }

    @Override
    public String getName() {
        return oldSourceFile.getName();
    }

    @Override
    public ICounter getInstructionCounter() {
        return oldSourceFile.getInstructionCounter();
    }

    @Override
    public ICounter getBranchCounter() {
        return oldSourceFile.getBranchCounter();
    }

    @Override
    public ICounter getLineCounter() {
        return oldSourceFile.getLineCounter();
    }

    @Override
    public ICounter getComplexityCounter() {
        return oldSourceFile.getComplexityCounter();
    }

    @Override
    public ICounter getMethodCounter() {
        return oldSourceFile.getMethodCounter();
    }

    @Override
    public ICounter getClassCounter() {
        return oldSourceFile.getClassCounter();
    }

    @Override
    public ICounter getCounter(CounterEntity entity) {
        return oldSourceFile.getCounter(entity);
    }

    @Override
    public boolean containsCode() {
        return oldSourceFile.containsCode();
    }

    @Override
    public ICoverageNode getPlainCopy() {
        return oldSourceFile.getPlainCopy();
    }
}
