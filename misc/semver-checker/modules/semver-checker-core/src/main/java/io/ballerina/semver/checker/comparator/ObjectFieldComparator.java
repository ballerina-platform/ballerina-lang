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
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.ObjectFieldDiff;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PRIVATE_KEYWORD;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.lookupQualifier;

/**
 * Comparator implementation for Ballerina object fields.
 *
 * @since 2201.2.0
 */
public class ObjectFieldComparator extends NodeComparator<ObjectFieldNode> {

    public ObjectFieldComparator(ObjectFieldNode newNode, ObjectFieldNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        NodeDiffBuilder diffBuilder = new ObjectFieldDiff.Builder(newNode, oldNode)
                .withChildDiffs(compareObjectFieldMetadata(newNode, oldNode))
                .withChildDiffs(compareObjectFieldQualifiers(newNode, oldNode))
                .withChildDiffs(compareObjectFieldType(newNode, oldNode))
                .withChildDiffs(compareObjectFieldExpression(newNode, oldNode));

        // if the object field is non-public, all the sub level changes can be considered as patch-compatible changes.
        if (!isPublic()) {
            diffBuilder = diffBuilder.withVersionImpact(SemverImpact.PATCH);
        }

        return diffBuilder.build();
    }

    private List<Diff> compareObjectFieldMetadata(ObjectFieldNode newNode, ObjectFieldNode oldNode) {
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
        DumbNodeListComparator<AnnotationNode> annotComparator = new DumbNodeListComparator<>(newAnnots, oldAnnots);
        annotComparator.computeDiff().ifPresent(metadataDiffs::add);

        return metadataDiffs;
    }

    private List<Diff> compareObjectFieldQualifiers(ObjectFieldNode newNode, ObjectFieldNode oldNode) {
        List<Diff> qualifierDiffs = new ArrayList<>();
        NodeList<Token> newQualifiers = newNode.qualifierList();
        NodeList<Token> oldQualifiers = oldNode.qualifierList();

        // analyzes public qualifier changes
        Optional<Token> newPublicQual = newNode.visibilityQualifier();
        Optional<Token> oldPublicQual = oldNode.visibilityQualifier();
        if (newPublicQual.isPresent() && oldPublicQual.isEmpty()) {
            // public qualifier added
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newPublicQual.get(), null);
            qualifierDiffBuilder
                    .withKind(DiffKind.OBJECT_FIELD)
                    .withVersionImpact(SemverImpact.MINOR)
                    .withMessage("'public' qualifier is added to object field '" + getObjectFieldName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newPublicQual.isEmpty() && oldPublicQual.isPresent()) {
            // public qualifier removed
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldPublicQual.get());
            qualifierDiffBuilder
                    .withKind(DiffKind.OBJECT_FIELD)
                    .withVersionImpact(SemverImpact.MAJOR)
                    .withMessage("'public' qualifier is removed from object field '" + getObjectFieldName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        // analyzes private qualifier changes
        Optional<Token> newIsolatedQual = lookupQualifier(newQualifiers, PRIVATE_KEYWORD);
        Optional<Token> oldIsolatedQual = lookupQualifier(oldQualifiers, PRIVATE_KEYWORD);
        if (newIsolatedQual.isPresent() && oldIsolatedQual.isEmpty()) {
            // private qualifier added
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newIsolatedQual.get(), null);
            qualifierDiffBuilder
                    .withKind(DiffKind.OBJECT_FIELD)
                    .withVersionImpact(SemverImpact.PATCH)
                    .withMessage("'private' qualifier is added to object field '" + getObjectFieldName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newIsolatedQual.isEmpty() && oldIsolatedQual.isPresent()) {
            // private qualifier removed
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldIsolatedQual.get());
            qualifierDiffBuilder
                    .withKind(DiffKind.OBJECT_FIELD)
                    .withVersionImpact(SemverImpact.PATCH)
                    .withMessage("'private' qualifier is removed from object field '" + getObjectFieldName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        // analyzes final qualifier changes
        Optional<Token> newTransactionalQual = lookupQualifier(newQualifiers, FINAL_KEYWORD);
        Optional<Token> oldTransactionalQual = lookupQualifier(oldQualifiers, FINAL_KEYWORD);
        if (newTransactionalQual.isPresent() && oldTransactionalQual.isEmpty()) {
            // transactional qualifier added
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newTransactionalQual.get(), null);
            qualifierDiffBuilder
                    .withKind(DiffKind.OBJECT_FIELD)
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'final' qualifier is added to object field '" + getObjectFieldName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newTransactionalQual.isEmpty() && oldTransactionalQual.isPresent()) {
            // final qualifier removed
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldTransactionalQual.get());
            qualifierDiffBuilder
                    .withKind(DiffKind.OBJECT_FIELD)
                    .withVersionImpact(SemverImpact.PATCH) // TODO: determine compatibility
                    .withMessage("'final' qualifier is removed from object field '" + getObjectFieldName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        return qualifierDiffs;
    }

    private List<Diff> compareObjectFieldType(ObjectFieldNode newNode, ObjectFieldNode oldNode) {
        List<Diff> typeDiffs = new LinkedList<>();
        Node newType = newNode.typeName();
        Node oldType = oldNode.typeName();

        if (newType == null || oldType == null) {
            return typeDiffs;
        } else if (!newType.toSourceCode().trim().equals(oldType.toSourceCode().trim())) {
            // Todo: improve type changes validation using semantic APIs
            NodeDiffBuilder diffBuilder = new NodeDiffImpl.Builder<>(newType, oldType);
            diffBuilder.withKind(DiffKind.OBJECT_FIELD)
                    .withVersionImpact(SemverImpact.AMBIGUOUS)
                    .withMessage(String.format("object field type changed from '%s' to '%s'",
                            oldType.toSourceCode().trim(), newType.toSourceCode().trim()))
                    .build().ifPresent(typeDiffs::add);
        }

        return typeDiffs;
    }

    private List<Diff> compareObjectFieldExpression(ObjectFieldNode newNode, ObjectFieldNode oldNode) {
        List<Diff> exprDiffs = new LinkedList<>();
        DumbNodeComparator<Node> exprComparator = new DumbNodeComparator<>(newNode.expression().orElse(null),
                oldNode.expression().orElse(null), DiffKind.OBJECT_FIELD_EXPR);
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

    private String getObjectFieldName() {
        return newNode != null ? newNode.fieldName().text() : oldNode.fieldName().text();
    }
}
