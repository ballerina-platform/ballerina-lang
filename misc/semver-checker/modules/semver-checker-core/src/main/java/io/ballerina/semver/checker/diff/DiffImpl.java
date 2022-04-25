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

package io.ballerina.semver.checker.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static io.ballerina.semver.checker.util.DiffUtils.stringifyDiff;

/**
 * Base implementation for all the diff types which can exist within a Ballerina package.
 *
 * @since 2201.2.0
 */
public class DiffImpl implements Diff {

    protected DiffType diffType;
    protected CompatibilityLevel compatibilityLevel;
    protected final List<Diff> childDiffs;

    public DiffImpl() {
        this(DiffType.UNKNOWN, CompatibilityLevel.UNKNOWN);
    }

    public DiffImpl(DiffType diffType, CompatibilityLevel compatibilityLevel) {
        this.diffType = diffType;
        this.compatibilityLevel = compatibilityLevel;
        this.childDiffs = new ArrayList<>();
    }

    @Override
    public DiffType getType() {
        return diffType;
    }

    @Override
    public void setType(DiffType diffType) {
        this.diffType = diffType;
    }

    @Override
    public CompatibilityLevel getCompatibilityLevel() {
        return compatibilityLevel;
    }

    @Override
    public void setCompatibilityLevel(CompatibilityLevel compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
    }

    @Override
    public void computeCompatibilityLevel() {
        if (compatibilityLevel == CompatibilityLevel.UNKNOWN) {
            compatibilityLevel = childDiffs.stream()
                    .map(Diff::getCompatibilityLevel)
                    .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                    .orElse(CompatibilityLevel.UNKNOWN);
        }
    }

    @Override
    public List<Diff> getChildDiffs() {
        return Collections.unmodifiableList(childDiffs);
    }

    @Override
    public List<Diff> getChildDiffs(CompatibilityLevel compatibilityLevel) {
        List<Diff> filteredDiffs = new ArrayList<>();
        for (Diff diff : childDiffs) {
            if (diff.getChildDiffs().isEmpty()) {
                if (diff.getCompatibilityLevel() == compatibilityLevel) {
                    filteredDiffs.add(diff);
                }
            } else {
                for (Diff childDiff : diff.getChildDiffs()) {
                    filteredDiffs.addAll(childDiff.getChildDiffs(compatibilityLevel));
                }
            }
        }
        return filteredDiffs;
    }

    @Override
    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stringifyDiff(this));
        if (diffType == DiffType.MODIFIED && childDiffs != null) {
            childDiffs.forEach(diff -> sb.append(diff.getAsString()));
        }
        return sb.toString();
    }
}
