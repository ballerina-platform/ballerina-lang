/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.syntax.tree;


/**
 * The {@code NodeTransformer} transform each node in the syntax tree to
 * another object of type T.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * This class allows you to transform the syntax tree into something else without
 * mutating instance variables.
 * <p>
 * There exists a transform method for each node in the Ballerina syntax tree.
 * These methods return T. If you are looking for a visitor that has visit
 * methods that return void, see {@link NodeVisitor}.
 *
 * This is a generated class.
 *
 * @param <T> the type of class that is returned by visit methods
 * @see NodeVisitor
 * @since 1.3.0
 */
public abstract class NodeTransformer<T> {

    public T transform(ModulePartNode modulePart) {
        return transformSyntaxNode(modulePart);
    }

    public T transform(FunctionDefinitionNode functionDefinition) {
        return transformSyntaxNode(functionDefinition);
    }

    public T transform(ImportDeclarationNode importDeclaration) {
        return transformSyntaxNode(importDeclaration);
    }

    public T transform(ListenerDeclarationNode listenerDeclaration) {
        return transformSyntaxNode(listenerDeclaration);
    }

    public T transform(TypeDefinitionNode typeDefinitionNode) {
        return transformSyntaxNode(typeDefinitionNode);
    }

    public T transform(ServiceDeclarationNode serviceDeclaration) {
        return transformSyntaxNode(serviceDeclaration);
    }

    public T transform(AssignmentStatementNode assignmentStatement) {
        return transformSyntaxNode(assignmentStatement);
    }

    public T transform(CompoundAssignmentStatementNode compoundAssignmentStatement) {
        return transformSyntaxNode(compoundAssignmentStatement);
    }

    public T transform(LocalTypeDefinitionStatementNode localTypeDefinitionStatement) {
        return transformSyntaxNode(localTypeDefinitionStatement);
    }

    public T transform(VariableDeclarationNode variableDeclaration) {
        return transformSyntaxNode(variableDeclaration);
    }

    public T transform(BlockStatementNode blockStatement) {
        return transformSyntaxNode(blockStatement);
    }

    public T transform(BreakStatementNode breakStatement) {
        return transformSyntaxNode(breakStatement);
    }

    public T transform(CallStatementNode callStatement) {
        return transformSyntaxNode(callStatement);
    }

    public T transform(ContinueStatementNode continueStatement) {
        return transformSyntaxNode(continueStatement);
    }

    public T transform(ExternalFunctionBodyNode externalFunctionBody) {
        return transformSyntaxNode(externalFunctionBody);
    }

    public T transform(IfElseStatementNode ifElseStatement) {
        return transformSyntaxNode(ifElseStatement);
    }

    public T transform(ElseBlockNode elseBlock) {
        return transformSyntaxNode(elseBlock);
    }

    public T transform(WhileStatementNode whileStatement) {
        return transformSyntaxNode(whileStatement);
    }

    public T transform(PanicStatementNode panicStatement) {
        return transformSyntaxNode(panicStatement);
    }

    public T transform(ReturnStatementNode returnStatement) {
        return transformSyntaxNode(returnStatement);
    }

    public T transform(BinaryExpressionNode binaryExpression) {
        return transformSyntaxNode(binaryExpression);
    }

    public T transform(BracedExpressionNode bracedExpression) {
        return transformSyntaxNode(bracedExpression);
    }

    public T transform(CheckExpressionNode checkExpression) {
        return transformSyntaxNode(checkExpression);
    }

    public T transform(FieldAccessExpressionNode fieldAccessExpression) {
        return transformSyntaxNode(fieldAccessExpression);
    }

    public T transform(FunctionCallExpressionNode functionCallExpression) {
        return transformSyntaxNode(functionCallExpression);
    }

    public T transform(MethodCallExpressionNode methodCallExpression) {
        return transformSyntaxNode(methodCallExpression);
    }

    public T transform(MappingConstructorExpressionNode mappingConstructorExpression) {
        return transformSyntaxNode(mappingConstructorExpression);
    }

    public T transform(MemberAccessExpressionNode memberAccessExpression) {
        return transformSyntaxNode(memberAccessExpression);
    }

    public T transform(TypeofExpressionNode typeofExpression) {
        return transformSyntaxNode(typeofExpression);
    }

    public T transform(UnaryExpressionNode unaryExpression) {
        return transformSyntaxNode(unaryExpression);
    }

    public T transform(ComputedNameFieldNode computedNameField) {
        return transformSyntaxNode(computedNameField);
    }

    public T transform(ConstantDeclarationNode constantDeclaration) {
        return transformSyntaxNode(constantDeclaration);
    }

    public T transform(DefaultableParameterNode defaultableParameter) {
        return transformSyntaxNode(defaultableParameter);
    }

    public T transform(RequiredParameterNode requiredParameter) {
        return transformSyntaxNode(requiredParameter);
    }

    public T transform(RestParameterNode restParameter) {
        return transformSyntaxNode(restParameter);
    }

    public T transform(ExpressionListItemNode expressionListItem) {
        return transformSyntaxNode(expressionListItem);
    }

    public T transform(ImportOrgNameNode importOrgName) {
        return transformSyntaxNode(importOrgName);
    }

    public T transform(ImportPrefixNode importPrefix) {
        return transformSyntaxNode(importPrefix);
    }

    public T transform(ImportSubVersionNode importSubVersion) {
        return transformSyntaxNode(importSubVersion);
    }

    public T transform(ImportVersionNode importVersion) {
        return transformSyntaxNode(importVersion);
    }

    public T transform(SubModuleNameNode subModuleName) {
        return transformSyntaxNode(subModuleName);
    }

    public T transform(SpecificFieldNode specificField) {
        return transformSyntaxNode(specificField);
    }

    public T transform(SpreadFieldNode spreadField) {
        return transformSyntaxNode(spreadField);
    }

    public T transform(NamedArgumentNode namedArgument) {
        return transformSyntaxNode(namedArgument);
    }

    public T transform(PositionalArgumentNode positionalArgument) {
        return transformSyntaxNode(positionalArgument);
    }

    public T transform(RestArgumentNode restArgument) {
        return transformSyntaxNode(restArgument);
    }

    public T transform(ObjectTypeDescriptorNode objectTypeDescriptor) {
        return transformSyntaxNode(objectTypeDescriptor);
    }

    public T transform(RecordTypeDescriptorNode recordTypeDescriptor) {
        return transformSyntaxNode(recordTypeDescriptor);
    }

    public T transform(ReturnTypeDescriptorNode returnTypeDescriptor) {
        return transformSyntaxNode(returnTypeDescriptor);
    }

    public T transform(NilTypeDescriptorNode nilTypeDescriptor) {
        return transformSyntaxNode(nilTypeDescriptor);
    }

    public T transform(OptionalTypeDescriptorNode optionalTypeDescriptor) {
        return transformSyntaxNode(optionalTypeDescriptor);
    }

    public T transform(ObjectFieldNode objectField) {
        return transformSyntaxNode(objectField);
    }

    public T transform(RecordFieldNode recordField) {
        return transformSyntaxNode(recordField);
    }

    public T transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValue) {
        return transformSyntaxNode(recordFieldWithDefaultValue);
    }

    public T transform(RecordRestDescriptorNode recordRestDescriptor) {
        return transformSyntaxNode(recordRestDescriptor);
    }

    public T transform(TypeReferenceNode typeReference) {
        return transformSyntaxNode(typeReference);
    }

    public T transform(QualifiedIdentifierNode qualifiedIdentifier) {
        return transformSyntaxNode(qualifiedIdentifier);
    }

    public T transform(ServiceBodyNode serviceBody) {
        return transformSyntaxNode(serviceBody);
    }

    public T transform(AnnotationNode annotationNode) {
        return transformSyntaxNode(annotationNode);
    }

    public T transform(MetadataNode metadata) {
        return transformSyntaxNode(metadata);
    }

    public T transform(ModuleVariableDeclarationNode moduleVariableDeclaration) {
        return transformSyntaxNode(moduleVariableDeclaration);
    }

    public T transform(IsExpressionNode isExpression) {
        return transformSyntaxNode(isExpression);
    }

    // Tokens

    public T transform(Token token) {
        return null;
    }

    public T transform(IdentifierToken identifier) {
        return null;
    }

    public T transform(EmptyToken emptyToken) {
        return null;
    }

    // Misc

    public T transform(EmptyNode emptyNode) {
        return transformSyntaxNode(emptyNode);
    }

    // TODO Why Minutiae is in this visitor? Check on this.
    public T transform(Minutiae minutiae) {
        return transformSyntaxNode(minutiae);
    }

    /**
     * Transforms the given {@code Node} into an object of type T.
     * <p>
     * This method is invoked by each transform method in this class. You can
     * override it to provide a common transformation for each node.
     *
     * @param node the {@code Node} to be transformed
     * @return the transformed object
     */
    protected abstract T transformSyntaxNode(Node node);
}

