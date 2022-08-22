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

package io.ballerina.semver.checker.comparator;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;
import io.ballerina.semver.checker.util.SyntaxTreeUtils;

import java.util.Optional;

/**
 * A concrete implementation of {@link NodeComparator}, which compares the two given syntax nodes only by the changes
 * in source code string.
 *
 * @param <T> node type
 * @since 2201.2.0
 */
public class DumbNodeComparator<T extends Node> implements Comparator {

    private final T newNode;
    private final T oldNode;
    private final DiffKind nodeKindName;

    DumbNodeComparator(T newNode, T oldNode) {
        this(newNode, oldNode, null);
    }

    DumbNodeComparator(T newNode, T oldNode, DiffKind nodeKindName) {
        this.newNode = newNode;
        this.oldNode = oldNode;
        this.nodeKindName = nodeKindName;
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        NodeDiffBuilder diffBuilder = new NodeDiffImpl.Builder<>(newNode, oldNode);
        if (nodeKindName != null) {
            diffBuilder.withKind(nodeKindName);
        }
        diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);
        if (newNode != null && oldNode == null) {
            diffBuilder.withMessage(String.format("a new %s is added",
                    nodeKindName != null ? nodeKindName : SyntaxTreeUtils.getNodeKindName(newNode.kind())));
            return diffBuilder.build();
        } else if (newNode == null && oldNode != null) {
            diffBuilder.withMessage(String.format("an existing %s is removed",
                    nodeKindName != null ? nodeKindName : SyntaxTreeUtils.getNodeKindName(oldNode.kind())));
            return diffBuilder.build();
        } else if (newNode == null) {
            return Optional.empty();
        } else if (!newNode.toSourceCode().trim().equals(oldNode.toSourceCode().trim())) {
            diffBuilder.withMessage(String.format("%s is modified",
                    nodeKindName != null ? nodeKindName : SyntaxTreeUtils.getNodeKindName(newNode.kind())));
            return diffBuilder.build();
        }

        return Optional.empty();
    }
}
