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
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.DiffType;
import io.ballerina.semver.checker.diff.NodeListDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getNodeKindName;

/**
 * A concrete implementation of {@link NodeListComparator}, which compares the two given syntax node lists only by the
 * changes in source code string.
 *
 * @param <T> node type
 * @since 2201.2.0
 */
public class DumbNodeListComparator<T extends Node> implements Comparator {

    private final List<T> newNodesList;
    private final List<T> oldNodesList;
    private final DiffKind nodeKind;

    DumbNodeListComparator(List<T> newNodesList, List<T> oldNodesList) {
        this(newNodesList, oldNodesList, null);
    }

    DumbNodeListComparator(NodeList<T> newNodesList, NodeList<T> oldNodesList) {
        this(newNodesList, oldNodesList, null);
    }

    DumbNodeListComparator(List<T> newNodesList, List<T> oldNodesList, DiffKind nodeKind) {
        this.newNodesList = newNodesList;
        this.oldNodesList = oldNodesList;
        this.nodeKind = nodeKind;
    }

    DumbNodeListComparator(NodeList<T> newNodesList, NodeList<T> oldNodesList, DiffKind nodeKind) {
        this.newNodesList = newNodesList != null ? newNodesList.stream().collect(Collectors.toList()) : null;
        this.oldNodesList = oldNodesList != null ? oldNodesList.stream().collect(Collectors.toList()) : null;
        this.nodeKind = nodeKind;
    }

    @Override
    public Optional<? extends NodeListDiffImpl<? extends Node>> computeDiff() {
        NodeListDiffImpl.Builder<?> diffBuilder = new NodeListDiffImpl.Builder<>(newNodesList, oldNodesList);
        diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);

        if (newNodesList != null && !newNodesList.isEmpty() && oldNodesList == null) {
            String kind = nodeKind != null ? nodeKind.toString() : getNodeKindName(newNodesList.get(0).kind());
            diffBuilder.withType(DiffType.NEW)
                    .withKind(this.nodeKind)
                    .withMessage(String.format("a new %s list is added", kind));
            return diffBuilder.build();
        } else if (newNodesList == null && oldNodesList != null && !oldNodesList.isEmpty()) {
            String kind = nodeKind != null ? nodeKind.toString() : getNodeKindName(oldNodesList.get(0).kind());
            diffBuilder.withType(DiffType.REMOVED)
                    .withKind(this.nodeKind)
                    .withMessage(String.format("an existing %s list is removed", kind));
            return diffBuilder.build();
        } else if ((newNodesList == null || newNodesList.isEmpty())
                && (oldNodesList == null || oldNodesList.isEmpty())) {
            return Optional.empty();
        } else if (!newNodesList.stream().map(Node::toSourceCode).collect(Collectors.joining(","))
                .equals(oldNodesList.stream().map(Node::toSourceCode).collect(Collectors.joining(",")))) {
            String kind = nodeKind != null ? nodeKind.toString() : getNodeKindName(newNodesList.get(0).kind());
            diffBuilder.withType(DiffType.MODIFIED)
                    .withKind(this.nodeKind)
                    .withMessage(String.format("%s list is modified", kind));
            return diffBuilder.build();
        }

        return Optional.empty();
    }
}
