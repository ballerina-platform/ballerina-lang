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
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.Optional;

/**
 * Comparator implementation to extract diffs from Ballerina documentations.
 *
 * @since 2201.2.0
 */
public class DocumentationComparator extends NodeComparator<Node> {

    public DocumentationComparator(Node newNode, Node oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        NodeDiffBuilder documentationDiffBuilder = new NodeDiffImpl.Builder<>(newNode, oldNode);
        if (newNode != null && oldNode == null) {
            return documentationDiffBuilder
                    .withVersionImpact(SemverImpact.PATCH)
                    .withMessage("documentation is added")
                    .build();
        } else if (newNode == null && oldNode != null) {
            return documentationDiffBuilder
                    .withVersionImpact(SemverImpact.PATCH)
                    .withMessage("documentation is removed")
                    .build();
        } else if (newNode == null) {
            return Optional.empty();
        } else if (newNode.toSourceCode().equals(oldNode.toSourceCode())) {
            return Optional.empty();
        } else {
            return documentationDiffBuilder
                    .withVersionImpact(SemverImpact.PATCH)
                    .withMessage("documentation is modified")
                    .build();
        }
    }
}
