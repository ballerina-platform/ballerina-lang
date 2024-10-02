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

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a coverage class containing normalized package and class names to reflect ballerina source root.
 *
 * @since 2.0.0
 */
public class NormalizedCoverageClass implements IClassCoverage {
    private final IClassCoverage oldClassCoverage;
    private final String normalizedPackageName;
    private final String normalizedClassName;
    private final Collection<IMethodCoverage> updatedMethods = new ArrayList<>();

    public NormalizedCoverageClass(IClassCoverage oldClassCoverage, String normalizedPackageName,
                                   String normalizedClassName) {
        this.oldClassCoverage = oldClassCoverage;
        this.normalizedPackageName = normalizedPackageName;
        this.normalizedClassName = normalizedClassName;
        for (IMethodCoverage oldMethodCoverage:oldClassCoverage.getMethods()) {
            updatedMethods.add(new NormalizedCoverageMethod(oldMethodCoverage));
        }
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
        // PackageName normalization is not done for non-ballerina classes and normalizedPackageName is null.
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
        return this.updatedMethods;
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
        // Sets the complexity value as 0 for ballerina source class coverages.
        return new PartialCoverageModifiedCounter(null);
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
        if (counterEntity.equals(CounterEntity.COMPLEXITY)) {
            // Sets the complexity value as 0 for ballerina class coverages.
            return new PartialCoverageModifiedCounter(null);
        }
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
