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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.ballerina.semver.checker.util.DiffUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_CHILDREN;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_KIND;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_TYPE;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_VERSION_IMPACT;
import static io.ballerina.semver.checker.util.DiffUtils.stringifyDiff;

/**
 * Base implementation for all the diff types which can exist within a Ballerina package.
 *
 * @since 2201.2.0
 */
public class DiffImpl implements Diff {

    protected DiffType diffType;
    protected SemverImpact versionImpact;
    protected final List<Diff> childDiffs;

    public DiffImpl() {
        this(DiffType.UNKNOWN, SemverImpact.UNKNOWN);
    }

    public DiffImpl(DiffType diffType, SemverImpact versionImpact) {
        this.diffType = diffType;
        this.versionImpact = versionImpact;
        this.childDiffs = new ArrayList<>();
    }

    @Override
    public DiffType getType() {
        return diffType;
    }

    protected void setType(DiffType diffType) {
        this.diffType = diffType;
    }

    @Override
    public SemverImpact getVersionImpact() {
        return versionImpact;
    }

    protected void setVersionImpact(SemverImpact versionImpact) {
        this.versionImpact = versionImpact;
    }

    @Override
    public void computeVersionImpact() {
        if (versionImpact == SemverImpact.UNKNOWN) {
            versionImpact = childDiffs.stream()
                    .map(Diff::getVersionImpact)
                    .max(Comparator.comparingInt(SemverImpact::getRank))
                    .orElse(SemverImpact.UNKNOWN);
        }
    }

    @Override
    public List<Diff> getChildDiffs() {
        return Collections.unmodifiableList(childDiffs);
    }

    @Override
    public List<Diff> getChildDiffs(SemverImpact versionImpact) {
        List<Diff> filteredDiffs = new ArrayList<>();
        for (Diff diff : childDiffs) {
            if (diff.getChildDiffs().isEmpty()) {
                if (diff.getVersionImpact() == versionImpact) {
                    filteredDiffs.add(diff);
                }
            } else {
                for (Diff childDiff : diff.getChildDiffs()) {
                    filteredDiffs.addAll(childDiff.getChildDiffs(versionImpact));
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

    @Override
    public JsonObject getAsJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(DIFF_ATTR_KIND, new JsonPrimitive(DiffUtils.getDiffTypeName(this)));
        jsonObject.add(DIFF_ATTR_TYPE, new JsonPrimitive(this.diffType.name().toLowerCase(Locale.getDefault())));
        jsonObject.add(DIFF_ATTR_VERSION_IMPACT, new JsonPrimitive(this.versionImpact.name()
                .toLowerCase(Locale.getDefault())));
        if (diffType == DiffType.MODIFIED && childDiffs != null && !childDiffs.isEmpty()) {
            JsonArray childArray = new JsonArray();
            childDiffs.forEach(diff -> childArray.add(diff.getAsJson()));
            jsonObject.add(DIFF_ATTR_CHILDREN, childArray);
        }

        return jsonObject;
    }
}
