/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.XMLNamespaceDeclarationNode;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

/**
 * A util class for mapping a given syntax node to the syntax node associated with its symbol (i.e., the name node)
 *
 * @since 2.0.0
 */
public class SyntaxNodeLocationMapper extends NodeTransformer<Optional<Location>> {

    // Nodes relevant for symbol()

    @Override
    public Optional<Location> transform(AnnotationNode annotationNode) {
        return annotationNode.annotReference().apply(this);
    }

    @Override
    public Optional<Location> transform(AnnotationDeclarationNode annotationDeclarationNode) {
        return Optional.of(annotationDeclarationNode.annotationTag().location());
    }

    @Override
    public Optional<Location> transform(ClassDefinitionNode classDefinitionNode) {
        return Optional.of(classDefinitionNode.className().location());
    }

    @Override
    public Optional<Location> transform(ConstantDeclarationNode constantDeclarationNode) {
        return Optional.of(constantDeclarationNode.variableName().location());
    }

    @Override
    public Optional<Location> transform(EnumDeclarationNode enumDeclarationNode) {
        return Optional.of(enumDeclarationNode.identifier().location());
    }

    @Override
    public Optional<Location> transform(EnumMemberNode enumMemberNode) {
        return Optional.of(enumMemberNode.identifier().location());
    }

    @Override
    public Optional<Location> transform(RecordFieldNode recordFieldNode) {
        return Optional.of(recordFieldNode.fieldName().location());
    }

    @Override
    public Optional<Location> transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        return Optional.of(recordFieldWithDefaultValueNode.fieldName().location());
    }

    @Override
    public Optional<Location> transform(ObjectFieldNode objectFieldNode) {
        return Optional.of(objectFieldNode.fieldName().location());
    }

    @Override
    public Optional<Location> transform(FunctionDefinitionNode functionDefinitionNode) {
        return Optional.of(functionDefinitionNode.functionName().location());
    }

    @Override
    public Optional<Location> transform(DefaultableParameterNode defaultableParameterNode) {
        if (defaultableParameterNode.paramName().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(defaultableParameterNode.paramName().get().location());
    }

    @Override
    public Optional<Location> transform(RequiredParameterNode requiredParameterNode) {
        if (requiredParameterNode.paramName().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(requiredParameterNode.paramName().get().location());
    }

    @Override
    public Optional<Location> transform(RestParameterNode restParameterNode) {
        if (restParameterNode.paramName().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(restParameterNode.paramName().get().location());
    }

    @Override
    public Optional<Location> transform(FunctionCallExpressionNode functionCallExpressionNode) {
        return Optional.of(functionCallExpressionNode.functionName().location());
    }

    @Override
    public Optional<Location> transform(MethodDeclarationNode methodDeclarationNode) {
        return Optional.of(methodDeclarationNode.methodName().location());
    }

    @Override
    public Optional<Location> transform(MethodCallExpressionNode methodCallExpressionNode) {
        return Optional.of(methodCallExpressionNode.methodName().location());
    }

    @Override
    public Optional<Location> transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        return Optional.of(remoteMethodCallActionNode.methodName().location());
    }

    @Override
    public Optional<Location> transform(ImportDeclarationNode importDeclarationNode) {
        if (importDeclarationNode.prefix().isPresent()) {
            return Optional.of(importDeclarationNode.prefix().get().location());
        }

        return Optional.of(
                importDeclarationNode.moduleName().get(importDeclarationNode.moduleName().size() - 1).location());
    }

    @Override
    public Optional<Location> transform(ImportPrefixNode importPrefixNode) {
        return Optional.of(importPrefixNode.location());
    }

    @Override
    public Optional<Location> transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        return Optional.of(qualifiedNameReferenceNode.identifier().location());
    }

    @Override
    public Optional<Location> transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        return Optional.of(simpleNameReferenceNode.name().location());
    }

    @Override
    public Optional<Location> transform(TypeDefinitionNode typeDefinitionNode) {
        return Optional.of(typeDefinitionNode.typeName().location());
    }

    @Override
    public Optional<Location> transform(VariableDeclarationNode variableDeclarationNode) {
        return variableDeclarationNode.typedBindingPattern().bindingPattern().apply(this);
    }

    @Override
    public Optional<Location> transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().apply(this);
    }

    @Override
    public Optional<Location> transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        return letVariableDeclarationNode.typedBindingPattern().bindingPattern().apply(this);
    }

    @Override
    public Optional<Location> transform(TypedBindingPatternNode typedBindingPatternNode) {
        return typedBindingPatternNode.bindingPattern().apply(this);
    }

    @Override
    public Optional<Location> transform(CaptureBindingPatternNode captureBindingPatternNode) {
        return Optional.of(captureBindingPatternNode.variableName().location());
    }

    @Override
    public Optional<Location> transform(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        return Optional.of(namedWorkerDeclarationNode.workerName().location());
    }

    @Override
    public Optional<Location> transform(XMLNamespaceDeclarationNode xmlNamespaceDeclarationNode) {
        if (xmlNamespaceDeclarationNode.namespacePrefix().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(xmlNamespaceDeclarationNode.namespacePrefix().get().location());
    }

    @Override
    public Optional<Location> transform(ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        if (moduleXMLNamespaceDeclarationNode.namespacePrefix().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(moduleXMLNamespaceDeclarationNode.namespacePrefix().get().location());
    }

    @Override
    protected Optional<Location> transformSyntaxNode(Node node) {
        return Optional.empty();
    }
}
