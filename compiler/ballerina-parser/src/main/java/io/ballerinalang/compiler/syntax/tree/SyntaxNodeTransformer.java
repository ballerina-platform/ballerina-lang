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
 * The {@code SyntaxNodeTransformer} transform each node in the syntax tree to
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
 * methods that return void, see {@link SyntaxNodeVisitor}.
 *
 * @param <T> the type of class that is returned by visit methods
 * @see SyntaxNodeVisitor
 * @since 1.3.0
 */
public abstract class SyntaxNodeTransformer<T> {

    public T transform(ModulePart modulePart) {
        return transformSyntaxNode(modulePart);
    }

    public T transform(FunctionDefinitionNode functionDefinitionNode) {
        return transformSyntaxNode(functionDefinitionNode);
    }

    public T transform(TypeDefinitionNode typeDefinitionNode) {
        return transformSyntaxNode(typeDefinitionNode);
    }

    public T transform(ImportDeclaration importDeclaration) {
        return transformSyntaxNode(importDeclaration);
    }

    // Statements

    public T transform(VariableDeclaration localVariableDeclaration) {
        return transformSyntaxNode(localVariableDeclaration);
    }

    public T transform(AssignmentStatement assignmentStatement) {
        return transformSyntaxNode(assignmentStatement);
    }

    public T transform(BlockStatement blockStatement) {
        return transformSyntaxNode(blockStatement);
    }

    public T transform(PanicStatement panicStatement) {
        return transformSyntaxNode(panicStatement);
    }

    public T transform(ContinueStatement continueStatement) {
        return transformSyntaxNode(continueStatement);
    }

    public T transform(BreakStatement breakStatement) {
        return transformSyntaxNode(breakStatement);
    }
    public T transform(ReturnStatement returnStatement) {
        return transformSyntaxNode(returnStatement);
    }

    // Expressions

    public T transform(BinaryExpression binaryExpression) {
        return transformSyntaxNode(binaryExpression);
    }

    public T transform(FunctionCallExpressionNode functionCallNode) {
        return transformSyntaxNode(functionCallNode);
    }

    public T transform(BracedExpression bracedExpression) {
        return transformSyntaxNode(bracedExpression);
    }

    // Tokens

    public T transform(Token token) {
        return null;
    }

    public T transform(Identifier identifier) {
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

    public T transform(RequiredParameter requiredParameter) {
        return transformSyntaxNode(requiredParameter);
    }

    public T transform(PositionalArgumentNode positionalArgumentNode) {
        return transformSyntaxNode(positionalArgumentNode);
    }

    public T transform(NamedArgumentNode namedArgumentNode) {
        return transformSyntaxNode(namedArgumentNode);
    }

    public T transform(RestArgumentNode restArgumentNode) {
        return transformSyntaxNode(restArgumentNode);
    }

    public T transform(ObjectFieldNode objectFieldNode) {
        return transformSyntaxNode(objectFieldNode);
    }

    public T transform(RecordFieldNode recordFieldNode) {
        return transformSyntaxNode(recordFieldNode);
    }

    public T transform(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        return transformSyntaxNode(recordFieldWithDefaultValueNode);
    }

    public T transform(RecordRestDescriptorNode recordRestDescriptorNode) {
        return transformSyntaxNode(recordRestDescriptorNode);
    }

    public T transform(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        return transformSyntaxNode(recordTypeDescriptorNode);
    }

    public T transform(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        return transformSyntaxNode(objectTypeDescriptorNode);
    }

    public T transform(TypeReferenceNode typeReferenceNode) {
        return transformSyntaxNode(typeReferenceNode);
    }

    public T transform(ImportPrefix importPrefix) {
        return transformSyntaxNode(importPrefix);
    }

    public T transform(ImportVersion importVersion) {
        return transformSyntaxNode(importVersion);
    }

    public T transform(ImportSubVersion importSubVersion) {
        return transformSyntaxNode(importSubVersion);
    }

    public T transform(SubModuleName subModuleName) {
        return transformSyntaxNode(subModuleName);
    }

    public T transform(ImportOrgName importOrgName) {
        return transformSyntaxNode(importOrgName);
    }

    public T transform(ComputedNameField computedNameField) {
        return transformSyntaxNode(computedNameField);
    }

    public T transform(MappingConstructorExpression mappingConstructorExpr) {
        return transformSyntaxNode(mappingConstructorExpr);
    }

    public T transform(SpecificField specificField) {
        return transformSyntaxNode(specificField);
    }

    public T transform(SpreadField spreadField) {
        return transformSyntaxNode(spreadField);
    }

    public T transform(ServiceBody serviceBody) {
        return transformSyntaxNode(serviceBody);
    }

    public T transform(ServiceDeclarationNode serviceDecl) {
        return transformSyntaxNode(serviceDecl);
    }

    public T transform(ExpressionListItem expressionListItem) {
        return transformSyntaxNode(expressionListItem);
    }

    public T transform(ListenerDeclaration listenerDecl) {
        return transformSyntaxNode(listenerDecl);
    }

    public T transform(ConstantDeclaration constantDecl) {
        return transformSyntaxNode(constantDecl);
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
