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

    protected final List<T> newNodesList;
    protected final List<T> oldNodesList;
    protected String nodeKindName;

    DumbNodeListComparator(List<T> newNodesList, List<T> oldNodesList) {
        this(newNodesList, oldNodesList, null);
    }

    DumbNodeListComparator(NodeList<T> newNodesList, NodeList<T> oldNodesList) {
        this(newNodesList, oldNodesList, null);
    }

    DumbNodeListComparator(List<T> newNodesList, List<T> oldNodesList, String nodeKindName) {
        this.newNodesList = newNodesList;
        this.oldNodesList = oldNodesList;
        this.nodeKindName = nodeKindName;
    }

    DumbNodeListComparator(NodeList<T> newNodesList, NodeList<T> oldNodesList, String nodeKindName) {
        this.newNodesList = newNodesList != null ? newNodesList.stream().collect(Collectors.toList()) : null;
        this.oldNodesList = oldNodesList != null ? oldNodesList.stream().collect(Collectors.toList()) : null;
        this.nodeKindName = nodeKindName;
    }

    @Override
    public Optional<? extends NodeListDiffImpl<? extends Node>> computeDiff() {
        NodeListDiffImpl.Builder<?> diffBuilder = new NodeListDiffImpl.Builder<>(newNodesList, oldNodesList);
        diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);

        if (newNodesList != null && !newNodesList.isEmpty() && oldNodesList == null) {
            String kind = nodeKindName != null ? nodeKindName : getNodeKindName(newNodesList.get(0).kind());
            diffBuilder.withType(DiffType.NEW).withMessage(String.format("a new %s list is added", kind));
            return diffBuilder.build();
        } else if (newNodesList == null && oldNodesList != null && !oldNodesList.isEmpty()) {
            String kind = nodeKindName != null ? nodeKindName : getNodeKindName(oldNodesList.get(0).kind());
            diffBuilder.withType(DiffType.REMOVED).withMessage(String.format("an existing %s list is removed", kind));
            return diffBuilder.build();
        } else if ((newNodesList == null || newNodesList.isEmpty())
                && (oldNodesList == null || oldNodesList.isEmpty())) {
            return Optional.empty();
        } else if (!newNodesList.stream().map(Node::toSourceCode).collect(Collectors.joining(","))
                .equals(oldNodesList.stream().map(Node::toSourceCode).collect(Collectors.joining(",")))) {
            String kind = nodeKindName != null ? nodeKindName : getNodeKindName(newNodesList.get(0).kind());
            diffBuilder.withType(DiffType.MODIFIED).withMessage(String.format("%s list is modified", kind));
            return diffBuilder.build();
        }

        return Optional.empty();
    }
}
