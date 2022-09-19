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
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.ModuleConstantDiff;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Comparator implementation for Ballerina module variable declarations.
 *
 * @since 2201.2.0
 */
public class ModuleConstantComparator extends NodeComparator<ConstantDeclarationNode> {

    public ModuleConstantComparator(ConstantDeclarationNode newNode, ConstantDeclarationNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        NodeDiffBuilder diffBuilder = new ModuleConstantDiff.Builder(newNode, oldNode)
                .withChildDiffs(compareModuleVariableMetadata(newNode, oldNode))
                .withChildDiffs(compareQualifiers(newNode, oldNode))
                .withChildDiffs(compareType(newNode, oldNode))
                .withChildDiffs(compareExpression(newNode, oldNode));

        // if the object field is non-public, all the sub level changes can be considered as patch-compatible changes.
        if (!isPublic()) {
            diffBuilder = diffBuilder.withVersionImpact(SemverImpact.PATCH);
        }

        return diffBuilder.build();
    }

    private List<Diff> compareModuleVariableMetadata(ConstantDeclarationNode newNode, ConstantDeclarationNode oldNode) {
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
                DiffKind.MODULE_CONST_ANNOTATION);
        annotComparator.computeDiff().ifPresent(metadataDiffs::add);

        return metadataDiffs;
    }

    private List<Diff> compareQualifiers(ConstantDeclarationNode newNode, ConstantDeclarationNode oldNode) {
        List<Diff> qualifierDiffs = new ArrayList<>();

        // analyzes public qualifier changes
        Optional<Token> newPublicQual = newNode.visibilityQualifier();
        Optional<Token> oldPublicQual = oldNode.visibilityQualifier();
        if (newPublicQual.isPresent() && oldPublicQual.isEmpty()) {
            // public qualifier added
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newPublicQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MINOR)
                    .withMessage("'public' qualifier is added to module constant '" + getModuleConstantName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newPublicQual.isEmpty() && oldPublicQual.isPresent()) {
            // public qualifier removed
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldPublicQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MAJOR)
                    .withMessage("'public' qualifier is removed from module constant '" + getModuleConstantName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        return qualifierDiffs;
    }

    private List<Diff> compareType(ConstantDeclarationNode newNode, ConstantDeclarationNode oldNode) {
        List<Diff> typeDiffs = new LinkedList<>();
        Optional<TypeDescriptorNode> newType = newNode.typeDescriptor();
        Optional<TypeDescriptorNode> oldType = oldNode.typeDescriptor();

        if (newType.isEmpty() && oldType.isEmpty()) {
            return typeDiffs;
        } else if (newType.isPresent() && oldType.isEmpty()) {
            NodeDiffBuilder diffBuilder = new NodeDiffImpl.Builder<>(newType.get(), null);
            diffBuilder = diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);
            diffBuilder.withMessage("module constant type added");
            diffBuilder.build().ifPresent(typeDiffs::add);
        } else if (newType.isEmpty()) {
            NodeDiffBuilder diffBuilder = new NodeDiffImpl.Builder<>(null, oldType.get());
            diffBuilder = diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);
            diffBuilder.withMessage("module constant type removed");
            diffBuilder.build().ifPresent(typeDiffs::add);
        } else if (!newType.get().toSourceCode().trim().equals(oldType.get().toSourceCode().trim())) {
            // Todo: improve type changes validation using semantic APIs
            NodeDiffBuilder diffBuilder = new NodeDiffImpl.Builder<>(newType.get(), oldType.get());
            diffBuilder = diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);
            diffBuilder.withMessage(String.format("module constant type changed from '%s' to '%s'",
                    oldType.get().toSourceCode().trim(), newType.get().toSourceCode().trim()));
            diffBuilder.build().ifPresent(typeDiffs::add);
        }

        return typeDiffs;
    }

    private List<Diff> compareExpression(ConstantDeclarationNode newNode, ConstantDeclarationNode oldNode) {
        List<Diff> exprDiffs = new LinkedList<>();
        DumbNodeComparator<Node> exprComparator = new DumbNodeComparator<>(newNode.initializer(),
                oldNode.initializer(), DiffKind.MODULE_CONST_INIT);
        exprComparator.computeDiff().ifPresent(exprDiffs::add);

        return exprDiffs;
    }

    private boolean isPublic() {
        boolean isNewPublic = newNode != null && newNode.visibilityQualifier().stream().anyMatch(qualifier ->
                qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD);
        boolean isOldPublic = oldNode != null && oldNode.visibilityQualifier().stream().anyMatch(qualifier ->
                qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD);

        return isNewPublic || isOldPublic;
    }

    private String getModuleConstantName() {
        return newNode != null ? newNode.variableName().toSourceCode().trim() :
                oldNode.variableName().toSourceCode().trim();
    }
}
