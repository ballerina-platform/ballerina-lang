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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.semver.checker.util.DiffUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_CHILDREN;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_KIND;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_MESSAGE;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_TYPE;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_VERSION_IMPACT;
import static io.ballerina.semver.checker.util.DiffUtils.stringifyDiff;

/**
 * Implementation of changes in Ballerina syntax tree nodes.
 *
 * @param <T> node type
 * @since 2201.2.0
 */
public class NodeDiffImpl<T extends Node> implements NodeDiff<T> {

    protected final T newNode;
    protected final T oldNode;
    protected DiffType diffType;
    protected SemverImpact versionImpact;
    protected final List<Diff> childDiffs;
    protected String message;

    protected NodeDiffImpl(T newNode, T oldNode) {
        this(newNode, oldNode, SemverImpact.UNKNOWN);
    }

    private NodeDiffImpl(T newNode, T oldNode, SemverImpact versionImpact) {
        this.newNode = newNode;
        this.oldNode = oldNode;
        this.versionImpact = versionImpact;
        this.childDiffs = new ArrayList<>();
        this.message = null;

        if (newNode != null && oldNode == null) {
            this.diffType = DiffType.NEW;
        } else if (newNode == null && oldNode != null) {
            this.diffType = DiffType.REMOVED;
        } else if (newNode != null) {
            this.diffType = DiffType.MODIFIED;
        } else {
            this.diffType = DiffType.UNKNOWN;
        }
    }

    @Override
    public Optional<T> getNewNode() {
        return Optional.ofNullable(newNode);
    }

    @Override
    public Optional<T> getOldNode() {
        return Optional.ofNullable(oldNode);
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
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
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

    protected void setMessage(String message) {
        this.message = message;
    }

    protected void setVersionImpact(SemverImpact versionImpact) {
        this.versionImpact = versionImpact;
    }

    @Override
    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        if (childDiffs == null || childDiffs.isEmpty()) {
            sb.append(stringifyDiff(this));
        } else {
            // Todo: Add the rest of module-level definition types
            if (this instanceof FunctionDiff) {
                sb.append(stringifyDiff(this));
            }
            childDiffs.forEach(diff -> sb.append(diff.getAsString()));
        }

        return sb.toString();
    }

    @Override
    public JsonObject getAsJson() {
        JsonObject jsonObject = new JsonObject();

        // Todo: Add the rest of module-level definition types
        if (childDiffs == null || childDiffs.isEmpty() || this instanceof FunctionDiff) {
            jsonObject.add(DIFF_ATTR_KIND, new JsonPrimitive(DiffUtils.getDiffTypeName(this)));
            jsonObject.add(DIFF_ATTR_TYPE, new JsonPrimitive(this.getType().name().toLowerCase(Locale.getDefault())));
            jsonObject.add(DIFF_ATTR_VERSION_IMPACT, new JsonPrimitive(this.getVersionImpact().name()
                    .toLowerCase(Locale.getDefault())));
        }

        if (this.getMessage().isPresent()) {
            jsonObject.add(DIFF_ATTR_MESSAGE, new JsonPrimitive(this.getMessage().get()));
        }

        if (childDiffs != null && !childDiffs.isEmpty()) {
            JsonArray childArray = new JsonArray();
            childDiffs.forEach(diff -> childArray.add(diff.getAsJson()));
            jsonObject.add(DIFF_ATTR_CHILDREN, childArray);
        }

        return jsonObject;
    }

    /**
     * Node diff builder implementation.
     *
     * @param <T> Node type
     */
    public static class Builder<T extends Node> implements NodeDiffBuilder {

        private final NodeDiffImpl<T> nodeDiff;

        public Builder(T newNode, T oldNode) {
            nodeDiff = new NodeDiffImpl<>(newNode, oldNode);
        }

        @Override
        public Optional<? extends NodeDiff<T>> build() {
            if (!nodeDiff.getChildDiffs().isEmpty()) {
                nodeDiff.computeVersionImpact();
                nodeDiff.setType(DiffType.MODIFIED);
                return Optional.of(nodeDiff);
            } else if (nodeDiff.getType() == DiffType.NEW || nodeDiff.getType() == DiffType.REMOVED
                    || nodeDiff.getMessage().isPresent()) {
                return Optional.of(nodeDiff);
            }

            return Optional.empty();
        }

        @Override
        public NodeDiffBuilder withType(DiffType diffType) {
            nodeDiff.setType(diffType);
            return this;
        }

        @Override
        public NodeDiffBuilder withVersionImpact(SemverImpact versionImpact) {
            nodeDiff.setVersionImpact(versionImpact);
            return this;
        }

        @Override
        public NodeDiffBuilder withMessage(String message) {
            nodeDiff.setMessage(message);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiff(Diff childDiff) {
            nodeDiff.childDiffs.add(childDiff);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiffs(Collection<? extends Diff> childDiffs) {
            nodeDiff.childDiffs.addAll(childDiffs);
            return this;
        }
    }
}
