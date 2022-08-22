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
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffExtractor;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.EnumDiff;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Comparator implementation for Ballerina enum declarations.
 *
 * @since 2201.2.0
 */
public class EnumComparator extends NodeComparator<EnumDeclarationNode> {

    public EnumComparator(EnumDeclarationNode newNode, EnumDeclarationNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        EnumDiff.Builder enumDiffBuilder = new EnumDiff.Builder(newNode, oldNode);
        return enumDiffBuilder
                .withChildDiffs(compareMetadata())
                .withChildDiffs(compareQualifiers())
                .withChildDiffs(compareEnumMembers())
                .build();
    }

    /**
     * Analyzes and returns the diff for changes on enum declaration metadata (documentation + annotations).
     */
    public List<Diff> compareMetadata() {
        List<Diff> metadataDiffs = new LinkedList<>();
        Optional<MetadataNode> newMeta = newNode.metadata();
        Optional<MetadataNode> oldMeta = oldNode.metadata();

        Node newDocs = newMeta.flatMap(MetadataNode::documentationString).orElse(null);
        Node oldDocs = oldMeta.flatMap(MetadataNode::documentationString).orElse(null);
        DocumentationComparator documentationComparator = new DocumentationComparator(newDocs, oldDocs);
        documentationComparator.computeDiff().ifPresent(metadataDiffs::add);

        NodeList<AnnotationNode> newAnnots = newMeta.map(MetadataNode::annotations).orElse(null);
        NodeList<AnnotationNode> oldAnnots = oldMeta.map(MetadataNode::annotations).orElse(null);
        DumbNodeListComparator<AnnotationNode> annotsComparator = new DumbNodeListComparator<>(newAnnots, oldAnnots,
                DiffKind.ENUM_ANNOTATION);
        annotsComparator.computeDiff().ifPresent(metadataDiffs::add);

        return metadataDiffs;
    }

    /**
     * Analyzes and returns the diff for changes on `public` qualifier.
     */
    private List<Diff> compareQualifiers() {
        List<Diff> qualifierDiffs = new ArrayList<>();

        // analyzes public qualifier changes
        Optional<Token> newPublicQual = newNode.qualifier();
        Optional<Token> oldPublicQual = oldNode.qualifier();
        if (newPublicQual.isPresent() && oldPublicQual.isEmpty()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newPublicQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MINOR)
                    .withMessage("'public' qualifier is added to enum '" + getEnumIdentifier() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newPublicQual.isEmpty() && oldPublicQual.isPresent()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldPublicQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MAJOR)
                    .withMessage("'public' qualifier is removed from enum '" + getEnumIdentifier() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        return qualifierDiffs;
    }

    private List<Diff> compareEnumMembers() {
        List<Diff> memberDiffs = new LinkedList<>();
        Map<String, EnumMemberNode> newMembers = newNode.enumMemberList().stream()
                .map(node -> (EnumMemberNode) node)
                .collect(Collectors.toMap(enumMemberNode -> enumMemberNode.identifier().text().trim(),
                        Function.identity()));
        Map<String, EnumMemberNode> oldMembers = oldNode.enumMemberList().stream()
                .map(node -> (EnumMemberNode) node)
                .collect(Collectors.toMap(enumMemberNode -> enumMemberNode.identifier().text().trim(),
                        Function.identity()));

        DiffExtractor<EnumMemberNode> memberDiffExtractor = new DiffExtractor<>(newMembers, oldMembers);

        // Computes and populate diffs for newly added enum members.
        memberDiffExtractor.getAdditions().forEach((enumName, enumNode) -> new NodeDiffImpl.Builder<>(enumNode, null)
                .withKind(DiffKind.ENUM_MEMBER)
                .withVersionImpact(SemverImpact.MINOR)
                .withMessage("new enum member '" + enumName + "' is added")
                .build().ifPresent(memberDiffs::add));

        // Computes and populate diffs for removed enum members.
        memberDiffExtractor.getRemovals().forEach((enumName, enumNode) -> new NodeDiffImpl.Builder<>(null, enumNode)
                .withKind(DiffKind.ENUM_MEMBER)
                .withVersionImpact(SemverImpact.MAJOR)
                .withMessage("new enum member '" + enumName + "' is removed")
                .build().ifPresent(memberDiffs::add));

        // Computes and populate diffs for modified enum members.
        memberDiffExtractor.getCommons().forEach((name, members) -> {
            EnumMemberComparator paramComparator = new EnumMemberComparator(members.getKey(), members.getValue());
            paramComparator.computeDiff().ifPresent(memberDiffs::add);
        });

        return memberDiffs;
    }

    private String getEnumIdentifier() {
        return newNode != null ? newNode.identifier().text().trim() :
                oldNode.identifier().text().trim();
    }
}
