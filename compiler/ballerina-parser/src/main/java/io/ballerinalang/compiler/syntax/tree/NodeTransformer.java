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

    public T transform(ModulePart modulePart) {
        return transformSyntaxNode(modulePart);
    }

    public T transform(FunctionDefinition functionDefinition) {
        return transformSyntaxNode(functionDefinition);
    }

    public T transform(ImportDeclaration importDeclaration) {
        return transformSyntaxNode(importDeclaration);
    }

    public T transform(ListenerDeclaration listenerDeclaration) {
        return transformSyntaxNode(listenerDeclaration);
    }

    public T transform(TypeDefinitionNode typeDefinitionNode) {
        return transformSyntaxNode(typeDefinitionNode);
    }

    public T transform(ServiceDeclaration serviceDeclaration) {
        return transformSyntaxNode(serviceDeclaration);
    }

    public T transform(AssignmentStatement assignmentStatement) {
        return transformSyntaxNode(assignmentStatement);
    }

    public T transform(CompoundAssignmentStatement compoundAssignmentStatement) {
        return transformSyntaxNode(compoundAssignmentStatement);
    }

    public T transform(VariableDeclaration variableDeclaration) {
        return transformSyntaxNode(variableDeclaration);
    }

    public T transform(BlockStatement blockStatement) {
        return transformSyntaxNode(blockStatement);
    }

    public T transform(BreakStatement breakStatement) {
        return transformSyntaxNode(breakStatement);
    }

    public T transform(CallStatement callStatement) {
        return transformSyntaxNode(callStatement);
    }

    public T transform(ContinueStatement continueStatement) {
        return transformSyntaxNode(continueStatement);
    }

    public T transform(ExternalFunctionBody externalFunctionBody) {
        return transformSyntaxNode(externalFunctionBody);
    }

    public T transform(IfElseStatement ifElseStatement) {
        return transformSyntaxNode(ifElseStatement);
    }

    public T transform(ElseBlock elseBlock) {
        return transformSyntaxNode(elseBlock);
    }

    public T transform(WhileStatement whileStatement) {
        return transformSyntaxNode(whileStatement);
    }

    public T transform(PanicStatement panicStatement) {
        return transformSyntaxNode(panicStatement);
    }

    public T transform(ReturnStatement returnStatement) {
        return transformSyntaxNode(returnStatement);
    }

    public T transform(BinaryExpression binaryExpression) {
        return transformSyntaxNode(binaryExpression);
    }

    public T transform(BracedExpression bracedExpression) {
        return transformSyntaxNode(bracedExpression);
    }

    public T transform(CheckExpression checkExpression) {
        return transformSyntaxNode(checkExpression);
    }

    public T transform(FieldAccessExpression fieldAccessExpression) {
        return transformSyntaxNode(fieldAccessExpression);
    }

    public T transform(FunctionCallExpression functionCallExpression) {
        return transformSyntaxNode(functionCallExpression);
    }

    public T transform(MethodCallExpression methodCallExpression) {
        return transformSyntaxNode(methodCallExpression);
    }

    public T transform(MappingConstructorExpression mappingConstructorExpression) {
        return transformSyntaxNode(mappingConstructorExpression);
    }

    public T transform(MemberAccessExpression memberAccessExpression) {
        return transformSyntaxNode(memberAccessExpression);
    }

    public T transform(TypeofExpression typeofExpression) {
        return transformSyntaxNode(typeofExpression);
    }

    public T transform(UnaryExpression unaryExpression) {
        return transformSyntaxNode(unaryExpression);
    }

    public T transform(ComputedNameField computedNameField) {
        return transformSyntaxNode(computedNameField);
    }

    public T transform(ConstantDeclaration constantDeclaration) {
        return transformSyntaxNode(constantDeclaration);
    }

    public T transform(DefaultableParameter defaultableParameter) {
        return transformSyntaxNode(defaultableParameter);
    }

    public T transform(RequiredParameter requiredParameter) {
        return transformSyntaxNode(requiredParameter);
    }

    public T transform(RestParameter restParameter) {
        return transformSyntaxNode(restParameter);
    }

    public T transform(ExpressionListItem expressionListItem) {
        return transformSyntaxNode(expressionListItem);
    }

    public T transform(ImportOrgName importOrgName) {
        return transformSyntaxNode(importOrgName);
    }

    public T transform(ImportPrefix importPrefix) {
        return transformSyntaxNode(importPrefix);
    }

    public T transform(ImportSubVersion importSubVersion) {
        return transformSyntaxNode(importSubVersion);
    }

    public T transform(ImportVersion importVersion) {
        return transformSyntaxNode(importVersion);
    }

    public T transform(SubModuleName subModuleName) {
        return transformSyntaxNode(subModuleName);
    }

    public T transform(SpecificField specificField) {
        return transformSyntaxNode(specificField);
    }

    public T transform(SpreadField spreadField) {
        return transformSyntaxNode(spreadField);
    }

    public T transform(NamedArgument namedArgument) {
        return transformSyntaxNode(namedArgument);
    }

    public T transform(PositionalArgument positionalArgument) {
        return transformSyntaxNode(positionalArgument);
    }

    public T transform(RestArgument restArgument) {
        return transformSyntaxNode(restArgument);
    }

    public T transform(ObjectTypeDescriptor objectTypeDescriptor) {
        return transformSyntaxNode(objectTypeDescriptor);
    }

    public T transform(RecordTypeDescriptor recordTypeDescriptor) {
        return transformSyntaxNode(recordTypeDescriptor);
    }

    public T transform(ReturnTypeDescriptor returnTypeDescriptor) {
        return transformSyntaxNode(returnTypeDescriptor);
    }

    public T transform(NilTypeDescriptor nilTypeDescriptor) {
        return transformSyntaxNode(nilTypeDescriptor);
    }

    public T transform(ObjectField objectField) {
        return transformSyntaxNode(objectField);
    }

    public T transform(RecordField recordField) {
        return transformSyntaxNode(recordField);
    }

    public T transform(RecordFieldWithDefaultValue recordFieldWithDefaultValue) {
        return transformSyntaxNode(recordFieldWithDefaultValue);
    }

    public T transform(RecordRestDescriptor recordRestDescriptor) {
        return transformSyntaxNode(recordRestDescriptor);
    }

    public T transform(TypeReference typeReference) {
        return transformSyntaxNode(typeReference);
    }

    public T transform(QualifiedIdentifier qualifiedIdentifier) {
        return transformSyntaxNode(qualifiedIdentifier);
    }

    public T transform(ServiceBody serviceBody) {
        return transformSyntaxNode(serviceBody);
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

