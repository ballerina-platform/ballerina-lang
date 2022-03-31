/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.semver.checker.diff;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Diff implements IDiff {

    protected DiffType diffType;
    protected CompatibilityLevel compatibilityLevel;
    protected final List<? extends Diff> childDiffs;

    public Diff() {
        this(DiffType.UNKNOWN, CompatibilityLevel.UNKNOWN);
    }

    public Diff(DiffType diffType, CompatibilityLevel compatibilityLevel) {
        this.diffType = diffType;
        this.compatibilityLevel = compatibilityLevel;
        this.childDiffs = new ArrayList<>();
    }

    public DiffType getType() {
        return diffType;
    }

    public void setType(DiffType diffType) {
        this.diffType = diffType;
    }

    public CompatibilityLevel getCompatibilityLevel() {
        if (compatibilityLevel == CompatibilityLevel.UNKNOWN) {
            compatibilityLevel = childDiffs.stream()
                    .map(Diff::getCompatibilityLevel)
                    .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                    .orElse(CompatibilityLevel.UNKNOWN);
        }

        return compatibilityLevel;
    }

    public void setCompatibilityLevel(CompatibilityLevel compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
    }
}
