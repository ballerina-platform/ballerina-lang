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
import io.ballerina.semver.checker.diff.IDiff;
import io.ballerina.semver.checker.diff.CompatibilityLevel;
import io.ballerina.semver.checker.diff.DiffType;
import io.ballerina.semver.checker.diff.FunctionDiff;
import io.ballerina.semver.checker.diff.NodeDiff;

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

        return Optional.of(functionDiffs);
    }

    /**
     * Analyzes and returns the diff for changes on function definition metadata (documentation + annotations)
     */
    public List<IDiff> compareMetadata() {
        List<IDiff> metadataDiffs = new ArrayList<>();
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
    private List<IDiff> compareFunctionQualifiers() {
        List<IDiff> qualifierDiffs = new ArrayList<>();
        NodeList<Token> newQualifiers = newNode.qualifierList();
        NodeList<Token> oldQualifiers = oldNode.qualifierList();

        // analyzes public qualifier changes
        Optional<Token> newPublicQual = getQualifier(newQualifiers, PUBLIC_KEYWORD);
        Optional<Token> oldPublicQual = getQualifier(oldQualifiers, PUBLIC_KEYWORD);
        if (newPublicQual.isEmpty() && oldPublicQual.isPresent()) {
            // public qualifier removed
            NodeDiff<Node> qualifierDiff = new NodeDiff<>(null, oldPublicQual.get());
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("function visibility is changed from 'public' to 'module-private'");
            qualifierDiffs.add(qualifierDiff);
        } else if (newPublicQual.isPresent() && oldPublicQual.isEmpty()) {
            // public qualifier added
            NodeDiff<Node> qualifierDiff = new NodeDiff<>(newPublicQual.get(), null);
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("function visibility is changed from 'module-private' to 'public'");
            qualifierDiffs.add(qualifierDiff);
        }

        // analyzes isolated qualifier changes
        Optional<Token> newIsolatedQual = getQualifier(newQualifiers, ISOLATED_KEYWORD);
        Optional<Token> oldIsolatedQual = getQualifier(oldQualifiers, ISOLATED_KEYWORD);
        if (newIsolatedQual.isEmpty() && oldIsolatedQual.isPresent()) {
            // isolated qualifier removed
            NodeDiff<Node> qualifierDiff = new NodeDiff<>(null, oldIsolatedQual.get());
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS); // Todo: determine compatibility
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("'isolated' qualifier is removed");
            qualifierDiffs.add(qualifierDiff);
        } else if (newIsolatedQual.isPresent() && oldIsolatedQual.isEmpty()) {
            // isolated qualifier added
            NodeDiff<Node> qualifierDiff = new NodeDiff<>(newIsolatedQual.get(), null);
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS); // Todo: determine compatibility
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("'isolated' qualifier is added");
            qualifierDiffs.add(qualifierDiff);
        }

        // analyzes transactional qualifier changes
        Optional<Token> newTransactionalQual = getQualifier(newQualifiers, TRANSACTIONAL_KEYWORD);
        Optional<Token> oldTransactionalQual = getQualifier(oldQualifiers, TRANSACTIONAL_KEYWORD);
        if (newTransactionalQual.isEmpty() && oldTransactionalQual.isPresent()) {
            // transactional qualifier removed
            NodeDiff<Node> qualifierDiff = new NodeDiff<>(null, oldTransactionalQual.get());
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS); // Todo: determine compatibility
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("'transactional' qualifier is removed");
            qualifierDiffs.add(qualifierDiff);
        } else if (newTransactionalQual.isPresent() && oldTransactionalQual.isEmpty()) {
            // transactional qualifier added
            NodeDiff<Node> qualifierDiff = new NodeDiff<>(newTransactionalQual.get(), null);
            qualifierDiff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS); // Todo: determine compatibility
            qualifierDiff.setType(DiffType.REMOVED);
            qualifierDiff.setMessage("'transactional' qualifier is added");
            qualifierDiffs.add(qualifierDiff);
        }

        return qualifierDiffs;
    }

    private List<IDiff> compareFunctionParams() {
        List<IDiff> paramDiffs = new ArrayList<>();
        List<ParameterNode> newParams = newNode.functionSignature().parameters().stream().collect(Collectors.toList());
        List<ParameterNode> oldParams = oldNode.functionSignature().parameters().stream().collect(Collectors.toList());

        ParamListComparator paramComparator = new ParamListComparator(newParams, oldParams);
        paramComparator.computeDiff().ifPresent(paramDiffs::add);

        return paramDiffs;
    }

    private List<NodeDiff<Node>> compareReturnTypeDesc() {
        List<NodeDiff<Node>> returnTypeDiffs = new ArrayList<>();
        Optional<ReturnTypeDescriptorNode> newReturn = newNode.functionSignature().returnTypeDesc();
        Optional<ReturnTypeDescriptorNode> oldReturn = oldNode.functionSignature().returnTypeDesc();

        if (newReturn.isPresent() && oldReturn.isEmpty()) {
            NodeDiff<Node> nodeDiff = new NodeDiff<>(newReturn.get(), null, DiffType.NEW,
                    CompatibilityLevel.MAJOR);
            nodeDiff.setMessage("return type is added to the function '" + newNode.functionName().text() + "'.");
        } else if (newReturn.isEmpty() && oldReturn.isPresent()) {
            NodeDiff<Node> nodeDiff = new NodeDiff<>(null, oldReturn.get(), DiffType.REMOVED,
                    CompatibilityLevel.MAJOR);
            nodeDiff.setMessage("return type is removed from the function '" + newNode.functionName().text() + "'.");
        } else if (newReturn.isPresent()) {
            compareReturnTypes(newReturn.get(), oldReturn.get()).ifPresent(returnTypeDiffs::add);
            compareReturnAnnotations(newReturn.get(), oldReturn.get()).ifPresent(returnTypeDiffs::add);
        }

        return returnTypeDiffs;
    }

    private Optional<NodeDiff<Node>> compareReturnTypes(ReturnTypeDescriptorNode newReturn,
                                                        ReturnTypeDescriptorNode oldReturn) {
        Node newType = newReturn.type();
        Node oldType = newReturn.type();

        if (!newType.toSourceCode().trim().equals(oldType.toSourceCode().trim())) {
            // Todo: improve type changes validation using semantic APIs
            NodeDiff<Node> diff = new NodeDiff<>(newReturn, oldReturn, DiffType.MODIFIED, CompatibilityLevel.AMBIGUOUS);
            diff.setMessage(String.format("return type is changed from: '%s' to: %s", oldType.toSourceCode(),
                    newType.toSourceCode()));
            return Optional.of(diff);
        }

        return Optional.empty();
    }

    private Optional<NodeDiff<Node>> compareReturnAnnotations(ReturnTypeDescriptorNode newReturn,
                                                              ReturnTypeDescriptorNode oldReturn) {
        NodeList<AnnotationNode> newAnnots = newReturn.annotations();
        NodeList<AnnotationNode> oldAnnots = oldReturn.annotations();

        // todo - implement comparison logic for return type annotations
        return Optional.empty();
    }

    private List<NodeDiff<Node>> compareFunctionBody() {
        List<NodeDiff<Node>> functionBodyDiff = new ArrayList<>();
        FunctionBodyNode newBody = newNode.functionBody();
        FunctionBodyNode oldBody = oldNode.functionBody();

        if ((newBody == null && oldBody != null) || (newBody != null && oldBody == null)) {
            functionBodyDiff.add(new NodeDiff<>(newBody, oldBody, DiffType.MODIFIED,
                    CompatibilityLevel.PATCH));
        } else if (newBody != null && !newBody.toSourceCode().equals(oldBody.toSourceCode())) {
            functionBodyDiff.add(new NodeDiff<>(newBody, oldBody, DiffType.MODIFIED,
                    CompatibilityLevel.PATCH));
        }

        return functionBodyDiff;
    }

    private Optional<Token> getQualifier(NodeList<Token> qualifierList, SyntaxKind qualifier) {
        return qualifierList.stream().filter(token -> token.kind() == qualifier).findAny();
    }
}
