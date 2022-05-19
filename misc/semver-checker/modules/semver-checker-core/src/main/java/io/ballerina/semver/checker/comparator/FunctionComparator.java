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
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.FunctionDiff;
import io.ballerina.semver.checker.diff.NodeDiff;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.NodeListDiff;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TRANSACTIONAL_KEYWORD;

/**
 * Comparator implementation for Ballerina function definitions.
 *
 * @since 2201.2.0
 */
public class FunctionComparator extends NodeComparator<FunctionDefinitionNode> {

    public FunctionComparator(FunctionDefinitionNode newNode, FunctionDefinitionNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        FunctionDiff.Builder funcDiffBuilder = new FunctionDiff.Builder(newNode, oldNode);
        return funcDiffBuilder
                .withChildDiffs(compareMetadata())
                .withChildDiffs(compareFunctionQualifiers())
                .withChildDiffs(compareFunctionParams())
                .withChildDiffs(compareReturnTypeDesc())
                .withChildDiffs(compareFunctionBody())
                .build();
    }

    /**
     * Analyzes and returns the diff for changes on function definition metadata (documentation + annotations).
     */
    public List<Diff> compareMetadata() {
        List<Diff> metadataDiffs = new ArrayList<>();
        Optional<MetadataNode> newMeta = newNode.metadata();
        Optional<MetadataNode> oldMeta = oldNode.metadata();

        Node newDocs = newMeta.flatMap(MetadataNode::documentationString).orElse(null);
        Node oldDocs = oldMeta.flatMap(MetadataNode::documentationString).orElse(null);
        DocumentationComparator documentationComparator = new DocumentationComparator(newDocs, oldDocs);
        documentationComparator.computeDiff().ifPresent(metadataDiffs::add);

        NodeList<AnnotationNode> newAnnots = newMeta.map(MetadataNode::annotations).orElse(null);
        NodeList<AnnotationNode> oldAnnots = oldMeta.map(MetadataNode::annotations).orElse(null);
        // todo - implement comparison logic for function annotations
        return metadataDiffs;
    }

    /**
     * Analyzes and returns the diff for changes on `public`, `isolated` and `transactional` qualifiers.
     */
    private List<Diff> compareFunctionQualifiers() {
        List<Diff> qualifierDiffs = new ArrayList<>();
        NodeList<Token> newQualifiers = newNode.qualifierList();
        NodeList<Token> oldQualifiers = oldNode.qualifierList();

        // analyzes public qualifier changes
        Optional<Token> newPublicQual = getQualifier(newQualifiers, PUBLIC_KEYWORD);
        Optional<Token> oldPublicQual = getQualifier(oldQualifiers, PUBLIC_KEYWORD);
        if (newPublicQual.isPresent() && oldPublicQual.isEmpty()) {
            // public qualifier added
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newPublicQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MINOR)
                    .withMessage("'public' qualifier is added")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newPublicQual.isEmpty() && oldPublicQual.isPresent()) {
            // public qualifier removed
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldPublicQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MAJOR)
                    .withMessage("'public' qualifier is removed")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        // analyzes isolated qualifier changes
        Optional<Token> newIsolatedQual = getQualifier(newQualifiers, ISOLATED_KEYWORD);
        Optional<Token> oldIsolatedQual = getQualifier(oldQualifiers, ISOLATED_KEYWORD);
        if (newIsolatedQual.isPresent() && oldIsolatedQual.isEmpty()) {
            // isolated qualifier added
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newIsolatedQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'isolated' qualifier is added")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newIsolatedQual.isEmpty() && oldIsolatedQual.isPresent()) {
            // isolated qualifier removed
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldIsolatedQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'isolated' qualifier is removed")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        // analyzes transactional qualifier changes
        Optional<Token> newTransactionalQual = getQualifier(newQualifiers, TRANSACTIONAL_KEYWORD);
        Optional<Token> oldTransactionalQual = getQualifier(oldQualifiers, TRANSACTIONAL_KEYWORD);
        if (newTransactionalQual.isPresent() && oldTransactionalQual.isEmpty()) {
            // transactional qualifier added
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newTransactionalQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'transactional' qualifier is added")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newTransactionalQual.isEmpty() && oldTransactionalQual.isPresent()) {
            // transactional qualifier removed
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldTransactionalQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'transactional' qualifier is removed")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        return qualifierDiffs;
    }

    private List<Diff> compareFunctionParams() {
        List<Diff> paramDiffs = new ArrayList<>();
        List<ParameterNode> newParams = newNode.functionSignature().parameters().stream().collect(Collectors.toList());
        List<ParameterNode> oldParams = oldNode.functionSignature().parameters().stream().collect(Collectors.toList());

        ParamListComparator paramComparator = new ParamListComparator(newParams, oldParams);
        paramComparator.computeDiff().ifPresent(diff -> {
            paramDiffs.addAll(extractTerminalDiffs(diff, new LinkedList<>()));
        });

        return paramDiffs;
    }

    private List<Diff> compareReturnTypeDesc() {
        List<Diff> returnTypeDiffs = new ArrayList<>();
        Optional<ReturnTypeDescriptorNode> newReturn = newNode.functionSignature().returnTypeDesc();
        Optional<ReturnTypeDescriptorNode> oldReturn = oldNode.functionSignature().returnTypeDesc();

        if (newReturn.isPresent() && oldReturn.isEmpty()) {
            NodeDiffBuilder returnDiffBuilder = new NodeDiffImpl.Builder<Node>(newReturn.get(), null);
            returnDiffBuilder
                    .withVersionImpact(SemverImpact.MAJOR)
                    .withMessage("return type is added")
                    .build()
                    .ifPresent(returnTypeDiffs::add);
        } else if (newReturn.isEmpty() && oldReturn.isPresent()) {
            NodeDiffBuilder returnDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldReturn.get());
            returnDiffBuilder
                    .withVersionImpact(SemverImpact.MAJOR)
                    .withMessage("return type is removed")
                    .build()
                    .ifPresent(returnTypeDiffs::add);
        } else if (newReturn.isPresent()) {
            compareReturnTypes(newReturn.get(), oldReturn.get()).ifPresent(returnTypeDiffs::add);
            compareReturnAnnotations(newReturn.get(), oldReturn.get()).ifPresent(returnTypeDiffs::add);
        }

        return returnTypeDiffs;
    }

    private Optional<? extends Diff> compareReturnTypes(ReturnTypeDescriptorNode newReturn,
                                                        ReturnTypeDescriptorNode oldReturn) {
        Node newType = newReturn.type();
        Node oldType = oldReturn.type();

        if (!newType.toSourceCode().trim().equals(oldType.toSourceCode().trim())) {
            // Todo: improve type changes validation using semantic APIs
            NodeDiffBuilder returnTypeDiffBuilder = new NodeDiffImpl.Builder<>(newType, oldType);
            return returnTypeDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS)
                    .withMessage(String.format("return type is changed from '%s' to '%s'", oldType.toSourceCode(),
                            newType.toSourceCode()))
                    .build();
        }

        return Optional.empty();
    }

    private Optional<NodeDiffImpl<Node>> compareReturnAnnotations(ReturnTypeDescriptorNode newReturn,
                                                                  ReturnTypeDescriptorNode oldReturn) {
        NodeList<AnnotationNode> newAnnots = newReturn.annotations();
        NodeList<AnnotationNode> oldAnnots = oldReturn.annotations();

        // todo - implement comparison logic for return type annotations
        return Optional.empty();
    }

    private List<Diff> compareFunctionBody() {
        List<Diff> functionBodyDiff = new ArrayList<>();
        FunctionBodyNode newBody = newNode.functionBody();
        FunctionBodyNode oldBody = oldNode.functionBody();

        NodeDiffBuilder functionBodyDiffBuilder = new NodeDiffImpl.Builder<>(newBody, oldBody);
        if (newBody != null && oldBody == null) {
            functionBodyDiffBuilder
                    .withVersionImpact(SemverImpact.PATCH)
                    .withMessage("function body is added")
                    .build()
                    .ifPresent(functionBodyDiff::add);
        } else if (newBody == null && oldBody != null) {
            functionBodyDiffBuilder
                    .withVersionImpact(SemverImpact.PATCH)
                    .withMessage("function body is removed")
                    .build()
                    .ifPresent(functionBodyDiff::add);
        } else {
            if (newBody != null && !newBody.toSourceCode().equals(oldBody.toSourceCode())) {
                functionBodyDiffBuilder
                        .withVersionImpact(SemverImpact.PATCH)
                        .withMessage("function body is modified")
                        .build()
                        .ifPresent(functionBodyDiff::add);
            }
        }

        return functionBodyDiff;
    }

    private Optional<Token> getQualifier(NodeList<Token> qualifierList, SyntaxKind qualifier) {
        return qualifierList.stream().filter(token -> token.kind() == qualifier).findAny();
    }

    private List<Diff> extractTerminalDiffs(Diff diff, List<Diff> extractedDiffs) {
        if (diff instanceof NodeDiff<?> && ((NodeDiff<?>) diff).getMessage().isPresent()) {
            extractedDiffs.add(diff);
        } else if (diff instanceof NodeListDiff<?> && ((NodeListDiff<?>) diff).getMessage().isPresent()) {
            extractedDiffs.add(diff);
        } else {
            for (Diff childDiff : diff.getChildDiffs()) {
                extractTerminalDiffs(childDiff, extractedDiffs);
            }
        }

        return extractedDiffs;
    }
}
