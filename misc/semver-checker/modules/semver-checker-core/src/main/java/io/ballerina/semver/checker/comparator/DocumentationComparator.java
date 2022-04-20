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
import io.ballerina.semver.checker.diff.CompatibilityLevel;
import io.ballerina.semver.checker.diff.DiffType;
import io.ballerina.semver.checker.diff.NodeDiff;

import java.util.Optional;

public class DocumentationComparator extends NodeComparator<Node> {

    public DocumentationComparator(Node newNode, Node oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<NodeDiff<Node>> computeDiff() {
        NodeDiff<Node> documentationDiff = new NodeDiff<>(newNode, oldNode);
        if (newNode != null && oldNode == null) {
            documentationDiff.setType(DiffType.NEW);
            documentationDiff.setCompatibilityLevel(CompatibilityLevel.PATCH);
            documentationDiff.setMessage("documentation is added.");
        } else if (newNode == null && oldNode != null) {
            documentationDiff.setType(DiffType.REMOVED);
            documentationDiff.setCompatibilityLevel(CompatibilityLevel.PATCH);
            documentationDiff.setMessage("documentation is removed.");
        } else if (newNode == null && oldNode == null) {
            return Optional.empty(); // unchanged
        } else if (newNode.toSourceCode().equals(oldNode.toSourceCode())) {
            return Optional.empty();  // unchanged
        } else {
            documentationDiff.setType(DiffType.MODIFIED);
            documentationDiff.setCompatibilityLevel(CompatibilityLevel.PATCH);
            documentationDiff.setMessage("documentation is modified.");
        }

        return Optional.of(documentationDiff);
    }
}
