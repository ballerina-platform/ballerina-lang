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
 * The {@code NodeVisitor} visits each node in the syntax tree allowing
 * us to do something at each node.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * {@code NodeVisitor} is a abstract class that itself visits the complete
 * tree. Subclasses have the ability to override only the required visit methods.
 * <p>
 * There exists a visit method for each node in the Ballerina syntax tree.
 * These methods return void. If you are looking for a visitor that has visit
 * methods that returns something, see {@link NodeTransformer}.
 *
 * This is a generated class.
 *
 * @see NodeTransformer
 * @since 1.3.0
 */
public abstract class NodeVisitor {

    public void visit(ModulePart modulePart) {
        visitSyntaxNode(modulePart);
    }

    public void visit(FunctionDefinition functionDefinition) {
        visitSyntaxNode(functionDefinition);
    }

    public void visit(ImportDeclaration importDeclaration) {
        visitSyntaxNode(importDeclaration);
    }

    public void visit(ListenerDeclaration listenerDeclaration) {
        visitSyntaxNode(listenerDeclaration);
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        visitSyntaxNode(typeDefinitionNode);
    }

    public void visit(ServiceDeclaration serviceDeclaration) {
        visitSyntaxNode(serviceDeclaration);
    }

    public void visit(AssignmentStatement assignmentStatement) {
        visitSyntaxNode(assignmentStatement);
    }

    public void visit(CompoundAssignmentStatement compoundAssignmentStatement) {
        visitSyntaxNode(compoundAssignmentStatement);
    }

    public void visit(VariableDeclaration variableDeclaration) {
        visitSyntaxNode(variableDeclaration);
    }

    public void visit(BlockStatement blockStatement) {
        visitSyntaxNode(blockStatement);
    }

    public void visit(BreakStatement breakStatement) {
        visitSyntaxNode(breakStatement);
    }

    public void visit(CallStatement callStatement) {
        visitSyntaxNode(callStatement);
    }

    public void visit(ContinueStatement continueStatement) {
        visitSyntaxNode(continueStatement);
    }

    public void visit(ExternalFunctionBody externalFunctionBody) {
        visitSyntaxNode(externalFunctionBody);
    }

    public void visit(IfElseStatement ifElseStatement) {
        visitSyntaxNode(ifElseStatement);
    }

    public void visit(ElseBlock elseBlock) {
        visitSyntaxNode(elseBlock);
    }

    public void visit(WhileStatement whileStatement) {
        visitSyntaxNode(whileStatement);
    }

    public void visit(PanicStatement panicStatement) {
        visitSyntaxNode(panicStatement);
    }

    public void visit(ReturnStatement returnStatement) {
        visitSyntaxNode(returnStatement);
    }

    public void visit(BinaryExpression binaryExpression) {
        visitSyntaxNode(binaryExpression);
    }

    public void visit(BracedExpression bracedExpression) {
        visitSyntaxNode(bracedExpression);
    }

    public void visit(CheckExpression checkExpression) {
        visitSyntaxNode(checkExpression);
    }

    public void visit(FieldAccessExpression fieldAccessExpression) {
        visitSyntaxNode(fieldAccessExpression);
    }

    public void visit(FunctionCallExpression functionCallExpression) {
        visitSyntaxNode(functionCallExpression);
    }

    public void visit(MethodCallExpression methodCallExpression) {
        visitSyntaxNode(methodCallExpression);
    }

    public void visit(MappingConstructorExpression mappingConstructorExpression) {
        visitSyntaxNode(mappingConstructorExpression);
    }

    public void visit(MemberAccessExpression memberAccessExpression) {
        visitSyntaxNode(memberAccessExpression);
    }

    public void visit(TypeofExpression typeofExpression) {
        visitSyntaxNode(typeofExpression);
    }

    public void visit(UnaryExpression unaryExpression) {
        visitSyntaxNode(unaryExpression);
    }

    public void visit(ComputedNameField computedNameField) {
        visitSyntaxNode(computedNameField);
    }

    public void visit(ConstantDeclaration constantDeclaration) {
        visitSyntaxNode(constantDeclaration);
    }

    public void visit(DefaultableParameter defaultableParameter) {
        visitSyntaxNode(defaultableParameter);
    }

    public void visit(RequiredParameter requiredParameter) {
        visitSyntaxNode(requiredParameter);
    }

    public void visit(RestParameter restParameter) {
        visitSyntaxNode(restParameter);
    }

    public void visit(ExpressionListItem expressionListItem) {
        visitSyntaxNode(expressionListItem);
    }

    public void visit(ImportOrgName importOrgName) {
        visitSyntaxNode(importOrgName);
    }

    public void visit(ImportPrefix importPrefix) {
        visitSyntaxNode(importPrefix);
    }

    public void visit(ImportSubVersion importSubVersion) {
        visitSyntaxNode(importSubVersion);
    }

    public void visit(ImportVersion importVersion) {
        visitSyntaxNode(importVersion);
    }

    public void visit(SubModuleName subModuleName) {
        visitSyntaxNode(subModuleName);
    }

    public void visit(SpecificField specificField) {
        visitSyntaxNode(specificField);
    }

    public void visit(SpreadField spreadField) {
        visitSyntaxNode(spreadField);
    }

    public void visit(NamedArgument namedArgument) {
        visitSyntaxNode(namedArgument);
    }

    public void visit(PositionalArgument positionalArgument) {
        visitSyntaxNode(positionalArgument);
    }

    public void visit(RestArgument restArgument) {
        visitSyntaxNode(restArgument);
    }

    public void visit(ObjectTypeDescriptor objectTypeDescriptor) {
        visitSyntaxNode(objectTypeDescriptor);
    }

    public void visit(RecordTypeDescriptor recordTypeDescriptor) {
        visitSyntaxNode(recordTypeDescriptor);
    }

    public void visit(ReturnTypeDescriptor returnTypeDescriptor) {
        visitSyntaxNode(returnTypeDescriptor);
    }

    public void visit(NilTypeDescriptor nilTypeDescriptor) {
        visitSyntaxNode(nilTypeDescriptor);
    }

    public void visit(ObjectField objectField) {
        visitSyntaxNode(objectField);
    }

    public void visit(RecordField recordField) {
        visitSyntaxNode(recordField);
    }

    public void visit(RecordFieldWithDefaultValue recordFieldWithDefaultValue) {
        visitSyntaxNode(recordFieldWithDefaultValue);
    }

    public void visit(RecordRestDescriptor recordRestDescriptor) {
        visitSyntaxNode(recordRestDescriptor);
    }

    public void visit(TypeReference typeReference) {
        visitSyntaxNode(typeReference);
    }

    public void visit(QualifiedIdentifier qualifiedIdentifier) {
        visitSyntaxNode(qualifiedIdentifier);
    }

    public void visit(ServiceBody serviceBody) {
        visitSyntaxNode(serviceBody);
    }

    public void visit(OptionalTypeDescriptor optionalTypeDescriptor) {
        visitSyntaxNode(optionalTypeDescriptor);
    }

    // Tokens

    public void visit(Token token) {
    }

    public void visit(EmptyToken emptyToken) {
    }

    // Misc

    public void visit(EmptyNode emptyNode) {
    }

    public void visit(Minutiae minutiae) {
    }

    public void visit(Annotation annotation) {
        visitSyntaxNode(annotation);
    }

    public void visit(Metadata metadata) {
        visitSyntaxNode(metadata);
    }

    public void visit(ModuleVariableDeclaration moduleVarDecl) {
        visitSyntaxNode(moduleVarDecl);
    }

    protected void visitSyntaxNode(Node node) {
        // TODO Find a better way to check for token
        if (node instanceof Token) {
            node.accept(this);
            return;
        }

        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        for (Node child : nonTerminalNode.children()) {
            child.accept(this);
        }
    }
}

