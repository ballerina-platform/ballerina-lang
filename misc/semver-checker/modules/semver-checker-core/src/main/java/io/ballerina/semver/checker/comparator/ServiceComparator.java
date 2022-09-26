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
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffExtractor;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.FunctionDiff;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;
import io.ballerina.semver.checker.diff.ServiceDiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getFunctionIdentifier;

/**
 * Comparator implementation for Ballerina service declarations.
 *
 * @since 2201.2.0
 */
public class ServiceComparator extends NodeComparator<ServiceDeclarationNode> {

    private final Map<String, FunctionDefinitionNode> newFunctions;
    private final Map<String, FunctionDefinitionNode> oldFunctions;
    private final Map<String, ObjectFieldNode> newServiceFields;
    private final Map<String, ObjectFieldNode> oldServiceFields;

    public ServiceComparator(ServiceDeclarationNode newNode, ServiceDeclarationNode oldNode) {
        super(newNode, oldNode);
        newFunctions = new HashMap<>();
        oldFunctions = new HashMap<>();
        newServiceFields = new HashMap<>();
        oldServiceFields = new HashMap<>();
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        ServiceDiff.Builder serviceDiffBuilder = new ServiceDiff.Builder(newNode, oldNode);
        return serviceDiffBuilder
                .withChildDiffs(compareMetadata())
                .withChildDiffs(compareServiceQualifiers())
                .withChildDiffs(compareServiceTypeDesc())
                .withChildDiffs(compareAttachPoints())
                .withChildDiffs(compareListenerExpressions())
                .withChildDiffs(compareMembers())
                .build();
    }

    /**
     * Analyzes and returns the diff for changes on service declaration metadata (documentation + annotations).
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
                DiffKind.SERVICE_ANNOTATION);
        annotsComparator.computeDiff().ifPresent(metadataDiffs::add);

        return metadataDiffs;
    }

    /**
     * Analyzes and returns the diff for changes on `public`, `isolated` and `transactional` qualifiers.
     */
    private List<Diff> compareServiceQualifiers() {
        List<Diff> qualifierDiffs = new ArrayList<>();
        NodeList<Token> newQualifiers = newNode.qualifiers();
        NodeList<Token> oldQualifiers = oldNode.qualifiers();

        // analyzes isolated qualifier changes
        Optional<Token> newIsolatedQual = newQualifiers.stream().filter(t -> t.kind() == ISOLATED_KEYWORD).findAny();
        Optional<Token> oldIsolatedQual = oldQualifiers.stream().filter(t -> t.kind() == ISOLATED_KEYWORD).findAny();
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

        return qualifierDiffs;
    }

    private List<Diff> compareServiceTypeDesc() {
        // TODO: implement service typedesc comparator
        Optional<? extends Diff> diff = new DumbNodeComparator<Node>(newNode.typeDescriptor().orElse(null),
                oldNode.typeDescriptor().orElse(null)).computeDiff();
        return diff.<List<Diff>>map(Collections::singletonList).orElseGet(ArrayList::new);
    }

    private List<Diff> compareAttachPoints() {
        // TODO: implement attach point comparator
        Optional<? extends Diff> diff = new DumbNodeListComparator<>(
                newNode.absoluteResourcePath().stream().collect(Collectors.toList()),
                oldNode.absoluteResourcePath().stream().collect(Collectors.toList()))
                .computeDiff();
        return diff.<List<Diff>>map(Collections::singletonList).orElseGet(ArrayList::new);
    }

    private List<Diff> compareListenerExpressions() {
        List<Diff> listenerDiffs = new LinkedList<>();
        // TODO: implement expression list comparator
        if (newNode.expressions().size() <= 1 && oldNode.expressions().size() <= 1) {
            ExpressionNode newListener = newNode.expressions().size() > 0 ? newNode.expressions().get(0) : null;
            ExpressionNode oldListener = oldNode.expressions().size() > 0 ? oldNode.expressions().get(0) : null;
            new DumbNodeComparator<>(newListener, oldListener, DiffKind.SERVICE_LISTENER_EXPR).computeDiff()
                    .ifPresent(listenerDiffs::add);
        } else {
            new DumbNodeListComparator<>(newNode.expressions().stream().collect(Collectors.toList()),
                    oldNode.expressions().stream().collect(Collectors.toList())).computeDiff()
                    .ifPresent(listenerDiffs::add);
        }

        return listenerDiffs;
    }

    private List<Diff> compareMembers() {
        List<Diff> memberDiffs = new LinkedList<>();
        // TODO: implement other service member comparators
        extractServiceMembers(newNode, true);
        extractServiceMembers(oldNode, false);

        DiffExtractor<FunctionDefinitionNode> resourceDiffExtractor = new DiffExtractor<>(newFunctions, oldFunctions);
        resourceDiffExtractor.getAdditions().forEach((name, function) -> {
            FunctionDiff.Builder funcDiffBuilder = new FunctionDiff.Builder(function, null);
            funcDiffBuilder.withVersionImpact(SemverImpact.MINOR).build().ifPresent(memberDiffs::add);
        });
        resourceDiffExtractor.getRemovals().forEach((name, function) -> {
            FunctionDiff.Builder funcDiffBuilder = new FunctionDiff.Builder(null, function);
            funcDiffBuilder.withVersionImpact(SemverImpact.MAJOR).build().ifPresent(memberDiffs::add);
        });
        resourceDiffExtractor.getCommons().forEach((name, functions) -> new FunctionComparator(functions.getKey(),
                functions.getValue()).computeDiff().ifPresent(memberDiffs::add));

        DiffExtractor<ObjectFieldNode> varDiffExtractor = new DiffExtractor<>(newServiceFields, oldServiceFields);
        varDiffExtractor.getAdditions().forEach((name, variable) -> {
            NodeDiffBuilder serviceVarDiffBuilder = new NodeDiffImpl.Builder<>(variable, null);
            serviceVarDiffBuilder.withVersionImpact(SemverImpact.MINOR).withKind(DiffKind.SERVICE_FIELD).build()
                    .ifPresent(memberDiffs::add);
        });
        varDiffExtractor.getRemovals().forEach((name, variable) -> {
            NodeDiffBuilder serviceVarDiffBuilder = new NodeDiffImpl.Builder<>(null, variable);
            serviceVarDiffBuilder.withVersionImpact(SemverImpact.MAJOR).withKind(DiffKind.SERVICE_FIELD).build()
                    .ifPresent(memberDiffs::add);
        });
        varDiffExtractor.getCommons().forEach((name, serviceVars) -> new ObjectFieldComparator(serviceVars.getKey(),
                serviceVars.getValue()).computeDiff().ifPresent(diff -> memberDiffs.addAll(diff.getChildDiffs())));

        return memberDiffs;
    }

    private void extractServiceMembers(ServiceDeclarationNode service, boolean isNewService) {
        service.members().forEach(member -> {
            switch (member.kind()) {
                case OBJECT_METHOD_DEFINITION:
                case RESOURCE_ACCESSOR_DEFINITION:
                    FunctionDefinitionNode funcNode = (FunctionDefinitionNode) member;
                    if (isNewService) {
                        newFunctions.put(getFunctionIdentifier(funcNode), funcNode);
                    } else {
                        oldFunctions.put(getFunctionIdentifier(funcNode), funcNode);
                    }
                    break;
                case OBJECT_FIELD:
                    ObjectFieldNode objectField = (ObjectFieldNode) member;
                    if (isNewService) {
                        newServiceFields.put(objectField.fieldName().toSourceCode(), objectField);
                    } else {
                        oldServiceFields.put(objectField.fieldName().toSourceCode(), objectField);
                    }
                    break;
                default:
                    // Todo: prompt a warning on the unsupported service member type
            }
        });
    }
}
