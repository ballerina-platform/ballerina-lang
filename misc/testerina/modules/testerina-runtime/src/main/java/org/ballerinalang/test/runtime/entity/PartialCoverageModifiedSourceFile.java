/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;

import java.util.List;

/**
 * Represents a source file containing lines modified to consider partially covered coverage info as fully covered.
 *
 * @since 2.0.0
 */
public class PartialCoverageModifiedSourceFile implements ISourceFileCoverage {

    private final ISourceFileCoverage oldSourceFile;
    private final List<ILine> modifiedLines;
    private final String normalizedPackageName;

    public PartialCoverageModifiedSourceFile(ISourceFileCoverage oldSourcefile, List<ILine> modifiedLines,
                                             String normalizedPackageName) {
        this.oldSourceFile = oldSourcefile;
        this.modifiedLines = modifiedLines;
        this.normalizedPackageName = normalizedPackageName;
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
    public ILine getLine(int lineNumber) {
        if (modifiedLines.isEmpty() || lineNumber < getFirstLine() || lineNumber > getLastLine()) {
            return oldSourceFile.getLine(lineNumber);
        }
        ILine reqLine = modifiedLines.get(lineNumber - getFirstLine());
        return reqLine == null ? oldSourceFile.getLine(lineNumber) : reqLine;
    }

    @Override
    public String getPackageName() {
        if (this.normalizedPackageName != null) {
            return this.normalizedPackageName;
        }
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
        // Sets the complexity value as 0 for ballerina source file coverages.
        return new PartialCoverageModifiedCounter(null);
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
        if (entity.equals(CounterEntity.COMPLEXITY)) {
            // Sets the complexity value as 0 for ballerina source file coverages.
            return new PartialCoverageModifiedCounter(null);
        }
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
