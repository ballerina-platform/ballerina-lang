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

import io.ballerina.compiler.syntax.tree.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
    protected CompatibilityLevel compatibilityLevel;
    protected final List<Diff> childDiffs;
    protected String message;

    protected NodeDiffImpl(T newNode, T oldNode) {
        this(newNode, oldNode, CompatibilityLevel.UNKNOWN);
    }

    private NodeDiffImpl(T newNode, T oldNode, CompatibilityLevel compatibilityLevel) {
        this.newNode = newNode;
        this.oldNode = oldNode;
        this.compatibilityLevel = compatibilityLevel;
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
    public CompatibilityLevel getCompatibilityLevel() {
        return compatibilityLevel;
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
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
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

    protected void setMessage(String message) {
        this.message = message;
    }

    protected void setCompatibilityLevel(CompatibilityLevel compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
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

    public static class Builder<T extends Node> implements NodeDiffBuilder {

        private final NodeDiffImpl<T> nodeDiff;

        public Builder(T newNode, T oldNode) {
            nodeDiff = new NodeDiffImpl<>(newNode, oldNode);
        }

        @Override
        public Optional<NodeDiff<?>> build() {
            if (!nodeDiff.getChildDiffs().isEmpty()) {
                nodeDiff.computeCompatibilityLevel();
                nodeDiff.setType(DiffType.MODIFIED);
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
        public NodeDiffBuilder withCompatibilityLevel(CompatibilityLevel compatibilityLevel) {
            nodeDiff.setCompatibilityLevel(compatibilityLevel);
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
