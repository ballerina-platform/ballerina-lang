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

    public NodeDiffImpl(T newNode, T oldNode) {
        this(newNode, oldNode, CompatibilityLevel.UNKNOWN);
    }

    public NodeDiffImpl(T newNode, T oldNode, CompatibilityLevel compatibilityLevel) {
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
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    public List<Diff> getChildDiffs() {
        return Collections.unmodifiableList(childDiffs);
    }

    public void addChildDiff(Diff childDiff) {
        this.childDiffs.add(childDiff);
    }

    public void addChildDiffs(List<? extends Diff> childDiffs) {
        this.childDiffs.addAll(childDiffs);
    }

    @Override
    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        if (childDiffs == null || childDiffs.isEmpty()) {
            sb.append(stringifyDiff(this));
        } else {
            childDiffs.forEach(diff -> sb.append(diff.getAsString()));
        }

        return sb.toString();
    }
}
