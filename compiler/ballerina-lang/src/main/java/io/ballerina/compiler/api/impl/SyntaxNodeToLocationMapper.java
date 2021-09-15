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

import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ByteArrayLiteralNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TransactionalExpressionNode;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceTypeDescNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

/**
 * A util class for mapping a given syntax node to the syntax node associated with its symbol (i.e., the name node).
 *
 * @since 2.0.0
 */
public class SyntaxNodeToLocationMapper extends NodeTransformer<Optional<Location>> {

    // Nodes relevant for symbol()

    @Override
    public Optional<Location> transform(AnnotationNode annotationNode) {
        return annotationNode.annotReference().apply(this);
    }

    @Override
    public Optional<Location> transform(AnnotationDeclarationNode annotationDeclarationNode) {
        return annotationDeclarationNode.annotationTag().apply(this);
    }

    @Override
    public Optional<Location> transform(ClassDefinitionNode classDefinitionNode) {
        return classDefinitionNode.className().apply(this);
    }

    @Override
    public Optional<Location> transform(ConstantDeclarationNode constantDeclarationNode) {
        return constantDeclarationNode.variableName().apply(this);
    }

    @Override
    public Optional<Location> transform(EnumDeclarationNode enumDeclarationNode) {
        return enumDeclarationNode.identifier().apply(this);
    }

    @Override
    public Optional<Location> transform(EnumMemberNode enumMemberNode) {
        return Optional.of(enumMemberNode.identifier().location());
    }

    @Override
    public Optional<Location> transform(RecordFieldNode recordFieldNode) {
        return recordFieldNode.fieldName().apply(this);
    }

    @Override
    public Optional<Location> transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        return recordFieldWithDefaultValueNode.fieldName().apply(this);
    }

    @Override
    public Optional<Location> transform(ObjectFieldNode objectFieldNode) {
        return objectFieldNode.fieldName().apply(this);
    }

    @Override
    public Optional<Location> transform(FunctionDefinitionNode functionDefinitionNode) {
        return functionDefinitionNode.functionName().apply(this);
    }

    @Override
    public Optional<Location> transform(DefaultableParameterNode defaultableParameterNode) {
        if (defaultableParameterNode.paramName().isEmpty()) {
            return Optional.empty();
        }

        return defaultableParameterNode.paramName().get().apply(this);
    }

    @Override
    public Optional<Location> transform(RequiredParameterNode requiredParameterNode) {
        if (requiredParameterNode.paramName().isEmpty()) {
            return Optional.empty();
        }

        return requiredParameterNode.paramName().get().apply(this);
    }

    @Override
    public Optional<Location> transform(RestParameterNode restParameterNode) {
        if (restParameterNode.paramName().isEmpty()) {
            return Optional.empty();
        }

        return restParameterNode.paramName().get().apply(this);
    }

    @Override
    public Optional<Location> transform(ResourcePathParameterNode resourcePathParameterNode) {
        return resourcePathParameterNode.paramName().apply(this);
    }

    @Override
    public Optional<Location> transform(FunctionCallExpressionNode functionCallExpressionNode) {
        return functionCallExpressionNode.functionName().apply(this);
    }

    @Override
    public Optional<Location> transform(MethodDeclarationNode methodDeclarationNode) {
        return methodDeclarationNode.methodName().apply(this);
    }

    @Override
    public Optional<Location> transform(MethodCallExpressionNode methodCallExpressionNode) {
        return methodCallExpressionNode.methodName().apply(this);
    }

    @Override
    public Optional<Location> transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        return remoteMethodCallActionNode.methodName().apply(this);
    }

    @Override
    public Optional<Location> transform(ImportDeclarationNode importDeclarationNode) {
        if (importDeclarationNode.prefix().isPresent()) {
            return importDeclarationNode.prefix().get().apply(this);
        }

        return importDeclarationNode.moduleName().get(importDeclarationNode.moduleName().size() - 1).apply(this);
    }

    @Override
    public Optional<Location> transform(ImportPrefixNode importPrefixNode) {
        return importPrefixNode.prefix().apply(this);
    }

    @Override
    public Optional<Location> transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        return qualifiedNameReferenceNode.identifier().apply(this);
    }

    @Override
    public Optional<Location> transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        return simpleNameReferenceNode.name().apply(this);
    }

    @Override
    public Optional<Location> transform(TypeDefinitionNode typeDefinitionNode) {
        return typeDefinitionNode.typeName().apply(this);
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
        return captureBindingPatternNode.variableName().apply(this);
    }

    @Override
    public Optional<Location> transform(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        return namedWorkerDeclarationNode.workerName().apply(this);
    }

    @Override
    public Optional<Location> transform(XMLNamespaceDeclarationNode xmlNamespaceDeclarationNode) {
        if (xmlNamespaceDeclarationNode.namespacePrefix().isEmpty()) {
            return Optional.empty();
        }

        return xmlNamespaceDeclarationNode.namespacePrefix().get().apply(this);
    }

    @Override
    public Optional<Location> transform(ModuleXMLNamespaceDeclarationNode moduleXMLNamespaceDeclarationNode) {
        if (moduleXMLNamespaceDeclarationNode.namespacePrefix().isEmpty()) {
            return Optional.empty();
        }

        return moduleXMLNamespaceDeclarationNode.namespacePrefix().get().apply(this);
    }

    @Override
    public Optional<Location> transform(TypeReferenceTypeDescNode typeReferenceTypeDescNode) {
        return typeReferenceTypeDescNode.typeRef().apply(this);
    }

    @Override
    public Optional<Location> transform(TypeReferenceNode typeReferenceNode) {
        return typeReferenceNode.typeName().apply(this);
    }

    @Override
    public Optional<Location> transform(ServiceDeclarationNode serviceDeclarationNode) {
        if (serviceDeclarationNode.qualifiers().isEmpty()) {
            return Optional.of(serviceDeclarationNode.serviceKeyword().location());
        }

        return serviceDeclarationNode.qualifiers().get(0).apply(this);
    }

    @Override
    public Optional<Location> transform(Token token) {
        return Optional.of(token.location());
    }

    @Override
    public Optional<Location> transform(IdentifierToken identifier) {
        return Optional.of(identifier.location());
    }

    // Nodes relevant for type()

    @Override
    public Optional<Location> transform(NilLiteralNode nilLiteralNode) {
        return Optional.of(nilLiteralNode.location());
    }

    @Override
    public Optional<Location> transform(BasicLiteralNode basicLiteralNode) {
        return basicLiteralNode.literalToken().apply(this);
    }

    @Override
    public Optional<Location> transform(ByteArrayLiteralNode byteArrayLiteralNode) {
        return Optional.of(byteArrayLiteralNode.location());
    }

    @Override
    public Optional<Location> transform(TemplateExpressionNode templateExpressionNode) {
        return Optional.of(templateExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        return Optional.of(listConstructorExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        return Optional.of(tableConstructorExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        return Optional.of(mappingConstructorExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(ObjectConstructorExpressionNode objectConstructorExpressionNode) {
        return Optional.of(objectConstructorExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(ExplicitNewExpressionNode explicitNewExpressionNode) {
        return Optional.of(explicitNewExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(ImplicitNewExpressionNode implicitNewExpressionNode) {
        return Optional.of(implicitNewExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        return builtinSimpleNameReferenceNode.name().apply(this);
    }

    @Override
    public Optional<Location> transform(FieldAccessExpressionNode fieldAccessExpressionNode) {
        return Optional.of(fieldAccessExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        return Optional.of(optionalFieldAccessExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(AnnotAccessExpressionNode annotAccessExpressionNode) {
        return Optional.of(annotAccessExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(IndexedExpressionNode indexedExpressionNode) {
        return Optional.of(indexedExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        return Optional.of(errorConstructorExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(
            ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        return Optional.of(explicitAnonymousFunctionExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(
            ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        return Optional.of(implicitAnonymousFunctionExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(LetExpressionNode letExpressionNode) {
        return Optional.of(letExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(TypeCastExpressionNode typeCastExpressionNode) {
        return Optional.of(typeCastExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(TypeofExpressionNode typeofExpressionNode) {
        return Optional.of(typeofExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(UnaryExpressionNode unaryExpressionNode) {
        return Optional.of(unaryExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(TypeTestExpressionNode typeTestExpressionNode) {
        return Optional.of(typeTestExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(BinaryExpressionNode binaryExpressionNode) {
        return Optional.of(binaryExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(ConditionalExpressionNode conditionalExpressionNode) {
        return Optional.of(conditionalExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(CheckExpressionNode checkExpressionNode) {
        return Optional.of(checkExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(TrapExpressionNode trapExpressionNode) {
        return Optional.of(trapExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(QueryExpressionNode queryExpressionNode) {
        return Optional.of(queryExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(XMLFilterExpressionNode xmlFilterExpressionNode) {
        return Optional.of(xmlFilterExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(XMLStepExpressionNode xmlStepExpressionNode) {
        return Optional.of(xmlStepExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(TransactionalExpressionNode transactionalExpressionNode) {
        return Optional.of(transactionalExpressionNode.location());
    }

    @Override
    public Optional<Location> transform(BracedExpressionNode bracedExpressionNode) {
        return bracedExpressionNode.expression().apply(this);
    }

    @Override
    public Optional<Location> transform(PositionalArgumentNode positionalArgumentNode) {
        return positionalArgumentNode.expression().apply(this);
    }

    @Override
    public Optional<Location> transform(SpecificFieldNode specificFieldNode) {
        return Optional.of(specificFieldNode.location());
    }

    @Override
    protected Optional<Location> transformSyntaxNode(Node node) {
        return Optional.empty();
    }
}
