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

package org.ballerinalang.semver.checker.comparator;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import org.ballerinalang.semver.checker.diff.FunctionDiff;
import org.ballerinalang.semver.checker.diff.NodeDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FunctionComparator extends NodeComparator<FunctionDefinitionNode> {

    public FunctionComparator(FunctionDefinitionNode newNode, FunctionDefinitionNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<FunctionDiff> computeDiff() {
        FunctionDiff functionDiffs = new FunctionDiff(newNode, oldNode);
        functionDiffs.addChildDiffs(analyzeMetadata());

        return Optional.of(functionDiffs);
    }

    public List<NodeDiff<Node>> analyzeMetadata() {
        List<NodeDiff<Node>> metadataDiffs = new ArrayList<>();
        Optional<MetadataNode> newMeta = newNode.metadata();
        Optional<MetadataNode> oldMeta = oldNode.metadata();

        Node newDocs = newMeta.flatMap(MetadataNode::documentationString).orElse(null);
        Node oldDocs = oldMeta.flatMap(MetadataNode::documentationString).orElse(null);
        DocumentationComparator documentationComparator = new DocumentationComparator(newDocs, oldDocs);
        documentationComparator.computeDiff().ifPresent(metadataDiffs::add);

        NodeList<AnnotationNode> newAnnots = newMeta.map(MetadataNode::annotations).orElse(null);
        NodeList<AnnotationNode> oldAnnots = oldMeta.map(MetadataNode::annotations).orElse(null);

        return metadataDiffs;
    }
}
