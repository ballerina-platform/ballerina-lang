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

    DumbNodeListComparator(List<T> newNodesList, List<T> oldNodesList) {
        this.newNodesList = newNodesList;
        this.oldNodesList = oldNodesList;
    }

    @Override
    public Optional<? extends NodeListDiffImpl<? extends Node>> computeDiff() {
        NodeListDiffImpl.Builder<?> diffBuilder = new NodeListDiffImpl.Builder<>(newNodesList, oldNodesList);
        diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);
        if (newNodesList != null && !newNodesList.isEmpty() && oldNodesList == null) {
            diffBuilder.withMessage(String.format("a new %s list is added",
                    getNodeKindName(newNodesList.get(0).kind())));
            return diffBuilder.build();
        } else if (newNodesList == null && oldNodesList != null && !oldNodesList.isEmpty()) {
            diffBuilder.withMessage(String.format("an existing %s list is removed",
                    getNodeKindName(oldNodesList.get(0).kind())));
            return diffBuilder.build();
        } else if ((newNodesList == null || newNodesList.isEmpty())
                && (oldNodesList == null || oldNodesList.isEmpty())) {
            return Optional.empty();
        } else if (!newNodesList.stream().map(Node::toSourceCode).collect(Collectors.joining(","))
                .equals(oldNodesList.stream().map(Node::toSourceCode).collect(Collectors.joining(",")))) {
            diffBuilder.withMessage(String.format("%s list is modified",
                    getNodeKindName(newNodesList.get(0).kind())));
            return diffBuilder.build();
        }

        return Optional.empty();
    }
}
