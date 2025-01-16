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
import org.jacoco.core.analysis.ILine;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a line modified to consider partially covered coverage info as fully covered.
 *
 * @since 2.0.0
 */
public class PartialCoverageModifiedLine implements ILine {

    private final PartialCoverageModifiedCounter instructions;
    private final PartialCoverageModifiedCounter branches;

    public PartialCoverageModifiedLine(@Nullable ICounter instructions, @Nullable ICounter branches) {
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

    /**
     * As implemented in org.jacoco.core.internal.analysis.LineImpl#getStatus().
     */
    @Override
    public int getStatus() {
        return branches.getStatus() | instructions.getStatus();
    }
}
