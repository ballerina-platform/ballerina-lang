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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.EnumMemberDiff;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Comparator implementation for Ballerina enum member declarations.
 *
 * @since 2201.2.0
 */
public class EnumMemberComparator extends NodeComparator<EnumMemberNode> {

    public EnumMemberComparator(EnumMemberNode newNode, EnumMemberNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        NodeDiffBuilder diffBuilder = new EnumMemberDiff.Builder(newNode, oldNode)
                .withChildDiffs(compareMetadata(newNode, oldNode))
                .withChildDiffs(compareExpression(newNode, oldNode));

        return diffBuilder.build();
    }

    private List<Diff> compareMetadata(EnumMemberNode newNode, EnumMemberNode oldNode) {
        List<Diff> metadataDiffs = new ArrayList<>();
        Optional<MetadataNode> newMeta = newNode.metadata();
        Optional<MetadataNode> oldMeta = oldNode.metadata();

        Node newDocs = newMeta.flatMap(MetadataNode::documentationString).orElse(null);
        Node oldDocs = oldMeta.flatMap(MetadataNode::documentationString).orElse(null);
        DocumentationComparator documentationComparator = new DocumentationComparator(newDocs, oldDocs);
        documentationComparator.computeDiff().ifPresent(metadataDiffs::add);

        NodeList<AnnotationNode> newAnnots = newMeta.map(MetadataNode::annotations).orElse(null);
        NodeList<AnnotationNode> oldAnnots = oldMeta.map(MetadataNode::annotations).orElse(null);
        // Replace with a smart comparator implementation
        DumbNodeListComparator<AnnotationNode> annotComparator = new DumbNodeListComparator<>(newAnnots, oldAnnots,
                DiffKind.ENUM_MEMBER_ANNOTATION);
        annotComparator.computeDiff().ifPresent(metadataDiffs::add);

        return metadataDiffs;
    }

    private List<Diff> compareExpression(EnumMemberNode newNode, EnumMemberNode oldNode) {
        List<Diff> memberExpressionDiffs = new ArrayList<>();
        DumbNodeComparator<ExpressionNode> exprComparator = new DumbNodeComparator<>(newNode.constExprNode()
                .orElse(null), oldNode.constExprNode().orElse(null), DiffKind.ENUM_MEMBER_VALUE);
        exprComparator.computeDiff().ifPresent(memberExpressionDiffs::add);

        return memberExpressionDiffs;
    }
}
