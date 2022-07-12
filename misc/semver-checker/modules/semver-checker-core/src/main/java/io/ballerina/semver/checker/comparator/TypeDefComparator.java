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
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;
import io.ballerina.semver.checker.diff.TypeDefinitionDiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Comparator implementation for Ballerina type definitions.
 *
 * @since 2201.2.0
 */
public class TypeDefComparator extends NodeComparator<TypeDefinitionNode> {

    public TypeDefComparator(TypeDefinitionNode newNode, TypeDefinitionNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        TypeDefinitionDiff.Builder typeDiffBuilder = new TypeDefinitionDiff.Builder(newNode, oldNode);
        return typeDiffBuilder
                .withChildDiffs(compareMetadata())
                .withChildDiffs(compareTypeDefQualifiers())
                .withChildDiffs(compareTypeDescriptor())
                .build();
    }

    /**
     * Analyzes and returns the diff for changes on type definition metadata (documentation + annotations).
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
                DiffKind.SERVICE_ANNOTATION.toString());
        annotsComparator.computeDiff().ifPresent(metadataDiffs::add);

        return metadataDiffs;
    }

    /**
     * Analyzes and returns the diff for changes on `public` qualifier.
     */
    private List<Diff> compareTypeDefQualifiers() {
        List<Diff> qualifierDiffs = new ArrayList<>();

        // analyzes public qualifier changes
        Optional<Token> newPublicQual = newNode.visibilityQualifier();
        Optional<Token> oldPublicQual = oldNode.visibilityQualifier();
        if (newPublicQual.isPresent() && oldPublicQual.isEmpty()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newPublicQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MINOR)
                    .withMessage("'public' qualifier is added to type '" + getTypeDefName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newPublicQual.isEmpty() && oldPublicQual.isPresent()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldPublicQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MAJOR)
                    .withMessage("'public' qualifier is removed from type '" + getTypeDefName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        return qualifierDiffs;
    }

    private List<Diff> compareTypeDescriptor() {
        Node newTypeDef = newNode.typeDescriptor();
        Node oldTypeDef = oldNode.typeDescriptor();
        // Todo: replace the dumb comparator with dedicated type descriptor comparators.
        DumbNodeComparator<Node> typeDescriptorComparator = new DumbNodeComparator<>(newTypeDef, oldTypeDef,
                DiffKind.TYPE_DESCRIPTOR);
        Optional<? extends Diff> diff = typeDescriptorComparator.computeDiff();
        if (diff.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(diff.get());
    }

    private String getTypeDefName() {
        return newNode != null ? newNode.typeName().text().trim() :
                oldNode.typeName().text().trim();
    }
}
