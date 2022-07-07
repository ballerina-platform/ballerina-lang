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
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.semver.checker.diff.ClassDiff;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffExtractor;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.FunctionDiff;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.ObjectFieldDiff;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DISTINCT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SERVICE_KEYWORD;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getFunctionIdentifier;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.lookupQualifier;

/**
 * Comparator implementation for Ballerina class definitions.
 *
 * @since 2201.2.0
 */
public class ClassComparator extends NodeComparator<ClassDefinitionNode> {

    private final Map<String, FunctionDefinitionNode> newFunctions;
    private final Map<String, FunctionDefinitionNode> oldFunctions;
    private final Map<String, ObjectFieldNode> newClassFields;
    private final Map<String, ObjectFieldNode> oldClassFields;

    public ClassComparator(ClassDefinitionNode newNode, ClassDefinitionNode oldNode) {
        super(newNode, oldNode);
        newFunctions = new HashMap<>();
        oldFunctions = new HashMap<>();
        newClassFields = new HashMap<>();
        oldClassFields = new HashMap<>();
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        ClassDiff.Builder classDiffBuilder = new ClassDiff.Builder(newNode, oldNode);
        return classDiffBuilder
                .withChildDiffs(compareMetadata())
                .withChildDiffs(compareClassQualifiers())
                .withChildDiffs(compareMembers())
                .build();
    }

    /**
     * Analyzes and returns the diff for changes on class declaration metadata (documentation + annotations).
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
     * Analyzes and returns the diff for changes on `public`, `isolated`, `distinct`, `readonly`, `client` and
     * `service` qualifiers.
     */
    private List<Diff> compareClassQualifiers() {
        List<Diff> qualifierDiffs = new ArrayList<>();

        // analyzes public qualifier changes
        Optional<Token> newPublicQual = newNode.visibilityQualifier();
        Optional<Token> oldPublicQual = oldNode.visibilityQualifier();
        if (newPublicQual.isPresent() && oldPublicQual.isEmpty()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newPublicQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MINOR)
                    .withMessage("'public' qualifier is added to class '" + getClassName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newPublicQual.isEmpty() && oldPublicQual.isPresent()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldPublicQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.MAJOR)
                    .withMessage("'public' qualifier is removed from class '" + getClassName() + "'")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        NodeList<Token> newQualifiers = newNode.classTypeQualifiers();
        NodeList<Token> oldQualifiers = oldNode.classTypeQualifiers();

        // analyzes isolated qualifier changes
        Optional<Token> newIsolatedQual = lookupQualifier(newQualifiers, ISOLATED_KEYWORD);
        Optional<Token> oldIsolatedQual = lookupQualifier(oldQualifiers, ISOLATED_KEYWORD);
        if (newIsolatedQual.isPresent() && oldIsolatedQual.isEmpty()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newIsolatedQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'isolated' qualifier is added")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newIsolatedQual.isEmpty() && oldIsolatedQual.isPresent()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldIsolatedQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'isolated' qualifier is removed")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        // analyzes distinct qualifier changes
        Optional<Token> newDistinctQual = lookupQualifier(newQualifiers, DISTINCT_KEYWORD);
        Optional<Token> oldDistinctQual = lookupQualifier(oldQualifiers, DISTINCT_KEYWORD);
        if (newDistinctQual.isPresent() && oldDistinctQual.isEmpty()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newDistinctQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'distinct' qualifier is added")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newDistinctQual.isEmpty() && oldDistinctQual.isPresent()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldDistinctQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'distinct' qualifier is removed")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        // analyzes readonly qualifier changes
        Optional<Token> newReadonlyQual = lookupQualifier(newQualifiers, READONLY_KEYWORD);
        Optional<Token> oldReadonlyQual = lookupQualifier(oldQualifiers, READONLY_KEYWORD);
        if (newReadonlyQual.isPresent() && oldReadonlyQual.isEmpty()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newReadonlyQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'readonly' qualifier is added")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newReadonlyQual.isEmpty() && oldReadonlyQual.isPresent()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldReadonlyQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'readonly' qualifier is removed")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        // analyzes client qualifier changes
        Optional<Token> newClientQual = lookupQualifier(newQualifiers, CLIENT_KEYWORD);
        Optional<Token> oldClientQual = lookupQualifier(oldQualifiers, CLIENT_KEYWORD);
        if (newClientQual.isPresent() && oldClientQual.isEmpty()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newClientQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'client' qualifier is added")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newClientQual.isEmpty() && oldClientQual.isPresent()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldClientQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'client' qualifier is removed")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }

        // analyzes service qualifier changes
        Optional<Token> newServiceQual = lookupQualifier(newQualifiers, SERVICE_KEYWORD);
        Optional<Token> oldServiceQual = lookupQualifier(oldQualifiers, SERVICE_KEYWORD);
        if (newServiceQual.isPresent() && oldServiceQual.isEmpty()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(newServiceQual.get(), null);
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'service' qualifier is added")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        } else if (newServiceQual.isEmpty() && oldServiceQual.isPresent()) {
            NodeDiffBuilder qualifierDiffBuilder = new NodeDiffImpl.Builder<Node>(null, oldServiceQual.get());
            qualifierDiffBuilder
                    .withVersionImpact(SemverImpact.AMBIGUOUS) // TODO: determine compatibility
                    .withMessage("'service' qualifier is removed")
                    .build()
                    .ifPresent(qualifierDiffs::add);
        }
        return qualifierDiffs;
    }

    private List<Diff> compareMembers() {
        List<Diff> memberDiffs = new LinkedList<>();
        extractClassMembers(newNode, true);
        extractClassMembers(oldNode, false);

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

        DiffExtractor<ObjectFieldNode> varDiffExtractor = new DiffExtractor<>(newClassFields, oldClassFields);
        varDiffExtractor.getAdditions().forEach((name, field) -> {
            NodeDiffBuilder classVarDiffBuilder = new ObjectFieldDiff.Builder(field, null);
            classVarDiffBuilder.withVersionImpact(SemverImpact.MINOR).withKind(DiffKind.OBJECT_FIELD).build()
                    .ifPresent(memberDiffs::add);
        });
        varDiffExtractor.getRemovals().forEach((name, field) -> {
            NodeDiffBuilder classVarDiffBuilder = new ObjectFieldDiff.Builder(null, field);
            classVarDiffBuilder.withVersionImpact(SemverImpact.MAJOR).withKind(DiffKind.OBJECT_FIELD).build()
                    .ifPresent(memberDiffs::add);
        });
        varDiffExtractor.getCommons().forEach((name, classFields) -> new ObjectFieldComparator(classFields.getKey(),
                classFields.getValue()).computeDiff().ifPresent(memberDiffs::add));

        return memberDiffs;
    }

    private void extractClassMembers(ClassDefinitionNode clazz, boolean isNewClass) {
        clazz.members().forEach(member -> {
            switch (member.kind()) {
                case OBJECT_METHOD_DEFINITION:
                case RESOURCE_ACCESSOR_DEFINITION:
                    FunctionDefinitionNode funcNode = (FunctionDefinitionNode) member;
                    if (isNewClass) {
                        newFunctions.put(getFunctionIdentifier(funcNode), funcNode);
                    } else {
                        oldFunctions.put(getFunctionIdentifier(funcNode), funcNode);
                    }
                    break;
                case OBJECT_FIELD:
                    ObjectFieldNode objectField = (ObjectFieldNode) member;
                    if (isNewClass) {
                        newClassFields.put(objectField.fieldName().text().trim(), objectField);
                    } else {
                        oldClassFields.put(objectField.fieldName().text().trim(), objectField);
                    }
                    break;
                default:
                    // Todo: prompt a warning on the unsupported class member types
            }
        });
    }

    private String getClassName() {
        return newNode != null ? newNode.className().text().trim() :
                oldNode.className().text().trim();
    }
}
