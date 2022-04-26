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
import io.ballerina.semver.checker.diff.CompatibilityLevel;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffType;
import io.ballerina.semver.checker.diff.FunctionDiff;
import io.ballerina.semver.checker.diff.NodeDiffImpl;

import java.util.ArrayList;
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
    public Optional<FunctionDiff> computeDiff() {
        FunctionDiff functionDiffs = new FunctionDiff(newNode, oldNode);
        functionDiffs.addChildDiffs(compareMetadata());
        functionDiffs.addChildDiffs(compareFunctionQualifiers());
        functionDiffs.addChildDiffs(compareFunctionParams());
        functionDiffs.addChildDiffs(compareReturnTypeDesc());
        functionDiffs.addChildDiffs(compareFunctionBody());

        if (!functionDiffs.getChildDiffs().isEmpty()) {
            functionDiffs.setType(DiffType.MODIFIED);
            functionDiffs.computeCompatibilityLevel();
            return Optional.of(functionDiffs);
        }

        return Optional.empty();
    }

    /**
     * Analyzes and returns the diff for changes on function definition metadata (documentation + annotations)
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
            NodeDiffImpl<Node> qualifierDiff = new NodeDiffImpl<>(newPublicQual.get(), null);
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.MINOR);
            qualifierDiff.setType(DiffType.NEW);
            qualifierDiff.setMessage("function visibility is changed from 'module-private' to 'public'");
            qualifierDiffs.add(qualifierDiff);
        } else if (newPublicQual.isEmpty() && oldPublicQual.isPresent()) {
            // public qualifier removed
            NodeDiffImpl<Node> qualifierDiff = new NodeDiffImpl<>(null, oldPublicQual.get());
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("function visibility is changed from 'public' to 'module-private'");
            qualifierDiffs.add(qualifierDiff);
        }

        // analyzes isolated qualifier changes
        Optional<Token> newIsolatedQual = getQualifier(newQualifiers, ISOLATED_KEYWORD);
        Optional<Token> oldIsolatedQual = getQualifier(oldQualifiers, ISOLATED_KEYWORD);
        if (newIsolatedQual.isPresent() && oldIsolatedQual.isEmpty()) {
            // isolated qualifier added
            NodeDiffImpl<Node> qualifierDiff = new NodeDiffImpl<>(newIsolatedQual.get(), null);
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS); // Todo: determine compatibility
            qualifierDiff.setType(DiffType.NEW);
            qualifierDiff.setMessage("'isolated' qualifier is added");
            qualifierDiffs.add(qualifierDiff);
        } else if (newIsolatedQual.isEmpty() && oldIsolatedQual.isPresent()) {
            // isolated qualifier removed
            NodeDiffImpl<Node> qualifierDiff = new NodeDiffImpl<>(null, oldIsolatedQual.get());
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS); // Todo: determine compatibility
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("'isolated' qualifier is removed");
            qualifierDiffs.add(qualifierDiff);
        }

        // analyzes transactional qualifier changes
        Optional<Token> newTransactionalQual = getQualifier(newQualifiers, TRANSACTIONAL_KEYWORD);
        Optional<Token> oldTransactionalQual = getQualifier(oldQualifiers, TRANSACTIONAL_KEYWORD);
        if (newTransactionalQual.isPresent() && oldTransactionalQual.isEmpty()) {
            // transactional qualifier added
            NodeDiffImpl<Node> qualifierDiff = new NodeDiffImpl<>(newTransactionalQual.get(), null);
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS); // Todo: determine compatibility
            qualifierDiff.setType(DiffType.NEW);
            qualifierDiff.setMessage("'transactional' qualifier is added");
            qualifierDiffs.add(qualifierDiff);
        } else if (newTransactionalQual.isEmpty() && oldTransactionalQual.isPresent()) {
            // transactional qualifier removed
            NodeDiffImpl<Node> qualifierDiff = new NodeDiffImpl<>(null, oldTransactionalQual.get());
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS); // Todo: determine compatibility
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("'transactional' qualifier is removed");
            qualifierDiffs.add(qualifierDiff);
        }

        return qualifierDiffs;
    }

    private List<Diff> compareFunctionParams() {
        List<Diff> paramDiffs = new ArrayList<>();
        List<ParameterNode> newParams = newNode.functionSignature().parameters().stream().collect(Collectors.toList());
        List<ParameterNode> oldParams = oldNode.functionSignature().parameters().stream().collect(Collectors.toList());

        ParamListComparator paramComparator = new ParamListComparator(newParams, oldParams);
        paramComparator.computeDiff().ifPresent(paramDiffs::add);

        return paramDiffs;
    }

    private List<NodeDiffImpl<Node>> compareReturnTypeDesc() {
        List<NodeDiffImpl<Node>> returnTypeDiffs = new ArrayList<>();
        Optional<ReturnTypeDescriptorNode> newReturn = newNode.functionSignature().returnTypeDesc();
        Optional<ReturnTypeDescriptorNode> oldReturn = oldNode.functionSignature().returnTypeDesc();

        if (newReturn.isPresent() && oldReturn.isEmpty()) {
            NodeDiffImpl<Node> nodeDiff = new NodeDiffImpl<>(newReturn.get(), null, CompatibilityLevel.MAJOR);
            nodeDiff.setMessage("return type is added to the function '" + newNode.functionName().text() + "'.");
        } else if (newReturn.isEmpty() && oldReturn.isPresent()) {
            NodeDiffImpl<Node> nodeDiff = new NodeDiffImpl<>(null, oldReturn.get(), CompatibilityLevel.MAJOR);
            nodeDiff.setMessage("return type is removed from the function '" + newNode.functionName().text() + "'.");
        } else if (newReturn.isPresent()) {
            compareReturnTypes(newReturn.get(), oldReturn.get()).ifPresent(returnTypeDiffs::add);
            compareReturnAnnotations(newReturn.get(), oldReturn.get()).ifPresent(returnTypeDiffs::add);
        }

        return returnTypeDiffs;
    }

    private Optional<NodeDiffImpl<Node>> compareReturnTypes(ReturnTypeDescriptorNode newReturn,
                                                            ReturnTypeDescriptorNode oldReturn) {
        Node newType = newReturn.type();
        Node oldType = newReturn.type();

        if (!newType.toSourceCode().trim().equals(oldType.toSourceCode().trim())) {
            // Todo: improve type changes validation using semantic APIs
            NodeDiffImpl<Node> diff = new NodeDiffImpl<>(newReturn, oldReturn, CompatibilityLevel.AMBIGUOUS);
            diff.setMessage(String.format("return type is changed from: '%s' to: %s", oldType.toSourceCode(),
                    newType.toSourceCode()));
            return Optional.of(diff);
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

    private List<NodeDiffImpl<Node>> compareFunctionBody() {
        List<NodeDiffImpl<Node>> functionBodyDiff = new ArrayList<>();
        FunctionBodyNode newBody = newNode.functionBody();
        FunctionBodyNode oldBody = oldNode.functionBody();

        if (newBody != null && oldBody == null) {
            NodeDiffImpl<Node> bodyDiff = new NodeDiffImpl<>(null, oldBody, CompatibilityLevel.PATCH);
            bodyDiff.setMessage("function body is added");
            functionBodyDiff.add(bodyDiff);
        } else if (newBody == null && oldBody != null) {
            NodeDiffImpl<Node> bodyDiff = new NodeDiffImpl<>(null, oldBody, CompatibilityLevel.PATCH);
            bodyDiff.setMessage("function body is removed");
            functionBodyDiff.add(bodyDiff);
        } else {
            if (newBody != null && !newBody.toSourceCode().equals(oldBody.toSourceCode())) {
                NodeDiffImpl<Node> bodyDiff = new NodeDiffImpl<>(newBody, oldBody, CompatibilityLevel.PATCH);
                bodyDiff.setMessage("function body is modified");
                functionBodyDiff.add(bodyDiff);
            }
        }

        return functionBodyDiff;
    }

    private Optional<Token> getQualifier(NodeList<Token> qualifierList, SyntaxKind qualifier) {
        return qualifierList.stream().filter(token -> token.kind() == qualifier).findAny();
    }
}
