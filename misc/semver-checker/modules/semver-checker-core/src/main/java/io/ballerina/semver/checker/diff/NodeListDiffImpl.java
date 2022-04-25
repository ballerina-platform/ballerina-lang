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
 * Base implementation of changes in Ballerina syntax tree node lists.
 *
 * @param <T> node type
 * @since 2201.2.0
 */
public class NodeListDiffImpl<T extends Node> implements NodeListDiff<List<T>> {

    protected final List<T> newNodes;
    protected final List<T> oldNodes;
    protected DiffType diffType;
    protected CompatibilityLevel compatibilityLevel;
    protected final List<NodeDiffImpl<? extends Node>> childDiffs;
    private String message;

    public NodeListDiffImpl(List<T> newNodes, List<T> oldNodes) {
        this(newNodes, oldNodes, DiffType.UNKNOWN, CompatibilityLevel.UNKNOWN);
    }

    public NodeListDiffImpl(List<T> newNodes, List<T> oldNodes, DiffType diffType,
                            CompatibilityLevel compatibilityLevel) {
        this.newNodes = newNodes;
        this.oldNodes = oldNodes;
        this.diffType = diffType;
        this.compatibilityLevel = compatibilityLevel;
        this.childDiffs = new ArrayList<>();
        this.message = null;
    }

    @Override
    public Optional<List<T>> getNewNodes() {
        return Optional.ofNullable(newNodes);
    }

    @Override
    public Optional<List<T>> getOldNodes() {
        return Optional.ofNullable(oldNodes);
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
                    .map(NodeDiffImpl::getCompatibilityLevel)
                    .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                    .orElse(CompatibilityLevel.UNKNOWN);
        }
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<NodeDiffImpl<? extends Node>> getChildDiffs() {
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

    public void addChildDiff(NodeDiffImpl<Node> childDiff) {
        this.childDiffs.add(childDiff);
    }

    public void addChildDiffs(List<NodeDiffImpl<Node>> childDiffs) {
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
