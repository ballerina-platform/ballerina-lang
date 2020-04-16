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

    public void visit(ModulePartNode modulePart) {
        visitSyntaxNode(modulePart);
    }

    public void visit(FunctionDefinitionNode functionDefinition) {
        visitSyntaxNode(functionDefinition);
    }

    public void visit(ImportDeclarationNode importDeclaration) {
        visitSyntaxNode(importDeclaration);
    }

    public void visit(ListenerDeclarationNode listenerDeclaration) {
        visitSyntaxNode(listenerDeclaration);
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        visitSyntaxNode(typeDefinitionNode);
    }

    public void visit(ServiceDeclarationNode serviceDeclaration) {
        visitSyntaxNode(serviceDeclaration);
    }

    public void visit(AssignmentStatementNode assignmentStatement) {
        visitSyntaxNode(assignmentStatement);
    }

    public void visit(CompoundAssignmentStatementNode compoundAssignmentStatement) {
        visitSyntaxNode(compoundAssignmentStatement);
    }

    public void visit(LocalTypeDefinitionStatementNode localTypeDefinitionStatement) {
        visitSyntaxNode(localTypeDefinitionStatement);
    }

    public void visit(VariableDeclarationNode variableDeclaration) {
        visitSyntaxNode(variableDeclaration);
    }

    public void visit(BlockStatementNode blockStatement) {
        visitSyntaxNode(blockStatement);
    }

    public void visit(BreakStatementNode breakStatement) {
        visitSyntaxNode(breakStatement);
    }

    public void visit(CallStatementNode callStatement) {
        visitSyntaxNode(callStatement);
    }

    public void visit(ContinueStatementNode continueStatement) {
        visitSyntaxNode(continueStatement);
    }

    public void visit(ExternalFunctionBodyNode externalFunctionBody) {
        visitSyntaxNode(externalFunctionBody);
    }

    public void visit(IfElseStatementNode ifElseStatement) {
        visitSyntaxNode(ifElseStatement);
    }

    public void visit(ElseBlockNode elseBlock) {
        visitSyntaxNode(elseBlock);
    }

    public void visit(WhileStatementNode whileStatement) {
        visitSyntaxNode(whileStatement);
    }

    public void visit(PanicStatementNode panicStatement) {
        visitSyntaxNode(panicStatement);
    }

    public void visit(ReturnStatementNode returnStatement) {
        visitSyntaxNode(returnStatement);
    }

    public void visit(BinaryExpressionNode binaryExpression) {
        visitSyntaxNode(binaryExpression);
    }

    public void visit(BracedExpressionNode bracedExpression) {
        visitSyntaxNode(bracedExpression);
    }

    public void visit(CheckExpressionNode checkExpression) {
        visitSyntaxNode(checkExpression);
    }

    public void visit(FieldAccessExpressionNode fieldAccessExpression) {
        visitSyntaxNode(fieldAccessExpression);
    }

    public void visit(FunctionCallExpressionNode functionCallExpression) {
        visitSyntaxNode(functionCallExpression);
    }

    public void visit(MethodCallExpressionNode methodCallExpression) {
        visitSyntaxNode(methodCallExpression);
    }

    public void visit(MappingConstructorExpressionNode mappingConstructorExpression) {
        visitSyntaxNode(mappingConstructorExpression);
    }

    public void visit(MemberAccessExpressionNode memberAccessExpression) {
        visitSyntaxNode(memberAccessExpression);
    }

    public void visit(TypeofExpressionNode typeofExpression) {
        visitSyntaxNode(typeofExpression);
    }

    public void visit(UnaryExpressionNode unaryExpression) {
        visitSyntaxNode(unaryExpression);
    }

    public void visit(ComputedNameFieldNode computedNameField) {
        visitSyntaxNode(computedNameField);
    }

    public void visit(ConstantDeclarationNode constantDeclaration) {
        visitSyntaxNode(constantDeclaration);
    }

    public void visit(DefaultableParameterNode defaultableParameter) {
        visitSyntaxNode(defaultableParameter);
    }

    public void visit(RequiredParameterNode requiredParameter) {
        visitSyntaxNode(requiredParameter);
    }

    public void visit(RestParameterNode restParameter) {
        visitSyntaxNode(restParameter);
    }

    public void visit(ExpressionListItemNode expressionListItem) {
        visitSyntaxNode(expressionListItem);
    }

    public void visit(ImportOrgNameNode importOrgName) {
        visitSyntaxNode(importOrgName);
    }

    public void visit(ImportPrefixNode importPrefix) {
        visitSyntaxNode(importPrefix);
    }

    public void visit(ImportSubVersionNode importSubVersion) {
        visitSyntaxNode(importSubVersion);
    }

    public void visit(ImportVersionNode importVersion) {
        visitSyntaxNode(importVersion);
    }

    public void visit(SubModuleNameNode subModuleName) {
        visitSyntaxNode(subModuleName);
    }

    public void visit(SpecificFieldNode specificField) {
        visitSyntaxNode(specificField);
    }

    public void visit(SpreadFieldNode spreadField) {
        visitSyntaxNode(spreadField);
    }

    public void visit(NamedArgumentNode namedArgument) {
        visitSyntaxNode(namedArgument);
    }

    public void visit(PositionalArgumentNode positionalArgument) {
        visitSyntaxNode(positionalArgument);
    }

    public void visit(RestArgumentNode restArgument) {
        visitSyntaxNode(restArgument);
    }

    public void visit(ObjectTypeDescriptorNode objectTypeDescriptor) {
        visitSyntaxNode(objectTypeDescriptor);
    }

    public void visit(RecordTypeDescriptorNode recordTypeDescriptor) {
        visitSyntaxNode(recordTypeDescriptor);
    }

    public void visit(ReturnTypeDescriptorNode returnTypeDescriptor) {
        visitSyntaxNode(returnTypeDescriptor);
    }

    public void visit(NilTypeDescriptorNode nilTypeDescriptor) {
        visitSyntaxNode(nilTypeDescriptor);
    }

    public void visit(OptionalTypeDescriptorNode optionalTypeDescriptor) {
        visitSyntaxNode(optionalTypeDescriptor);
    }

    public void visit(ObjectFieldNode objectField) {
        visitSyntaxNode(objectField);
    }

    public void visit(RecordFieldNode recordField) {
        visitSyntaxNode(recordField);
    }

    public void visit(RecordFieldWithDefaultValueNode recordFieldWithDefaultValue) {
        visitSyntaxNode(recordFieldWithDefaultValue);
    }

    public void visit(RecordRestDescriptorNode recordRestDescriptor) {
        visitSyntaxNode(recordRestDescriptor);
    }

    public void visit(TypeReferenceNode typeReference) {
        visitSyntaxNode(typeReference);
    }

    public void visit(QualifiedIdentifierNode qualifiedIdentifier) {
        visitSyntaxNode(qualifiedIdentifier);
    }

    public void visit(ServiceBodyNode serviceBody) {
        visitSyntaxNode(serviceBody);
    }

    public void visit(AnnotationNode annotationNode) {
        visitSyntaxNode(annotationNode);
    }

    public void visit(MetadataNode metadata) {
        visitSyntaxNode(metadata);
    }

    public void visit(ModuleVariableDeclarationNode moduleVariableDeclaration) {
        visitSyntaxNode(moduleVariableDeclaration);
    }

    public void visit(IsExpressionNode isExpression) {
        visitSyntaxNode(isExpression);
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

