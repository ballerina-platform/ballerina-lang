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

import java.util.List;
import java.util.Optional;

/**
 * Abstract definition of the Ballerina syntax tree node list based comparators.
 *
 * @param <T> node type
 * @since 2201.2.0
 */
public abstract class NodeListComparator<T extends List<? extends Node>> implements Comparator {

    protected final T newNodesList;
    protected final T oldNodesList;

    NodeListComparator(T newNodesList, T oldNodesList) {
        this.newNodesList = newNodesList;
        this.oldNodesList = oldNodesList;
    }

    @Override
    public abstract Optional<? extends NodeListDiffImpl<? extends Node>> computeDiff();
}
