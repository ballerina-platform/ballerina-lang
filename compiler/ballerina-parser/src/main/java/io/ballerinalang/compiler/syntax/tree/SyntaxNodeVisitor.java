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
 * The {@code SyntaxNodeVisitor} visits each node in the syntax tree allowing
 * us to do something at each node.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * {@code SyntaxNodeVisitor} is a abstract class that itself visits the complete
 * tree. Subclasses have the ability to override only the required visit methods.
 * <p>
 * There exists a visit method for each node in the Ballerina syntax tree.
 * These methods return void. If you are looking for a visitor that has visit
 * methods that returns something, see {@link SyntaxNodeTransformer}.
 *
 * @see SyntaxNodeTransformer
 * @since 1.3.0
 */
public abstract class SyntaxNodeVisitor {
    public void visit(ModulePart modulePart) {
        visitSyntaxNode(modulePart);
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        visitSyntaxNode(functionDefinitionNode);
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        visitSyntaxNode(typeDefinitionNode);
    }

    public void visit(ImportDeclaration importDeclaration) {
        visitSyntaxNode(importDeclaration);
    }

    // Statements

    public void visit(VariableDeclaration localVariableDeclaration) {
        visitSyntaxNode(localVariableDeclaration);
    }

    public void visit(AssignmentStatement assignmentStatement) {
        visitSyntaxNode(assignmentStatement);
    }

    public void visit(BlockStatement blockStatement) {
        visitSyntaxNode(blockStatement);
    }

    public void visit(PanicStatement panicStatement) {
        visitSyntaxNode(panicStatement);
    }

    public void visit(ContinueStatement continueStatement) {
        visitSyntaxNode(continueStatement);
    }

    public void visit(BreakStatement breakStatement) {
        visitSyntaxNode(breakStatement);
    }

    public void visit(ReturnStatement returnStatement) {
        visitSyntaxNode(returnStatement);
    }

    public void visit(CompoundAssignmentStatement compoundAssignmentStatement) {
        visitSyntaxNode(compoundAssignmentStatement);
    }

    // Expressions

    public void visit(BinaryExpression binaryExpression) {
        visitSyntaxNode(binaryExpression);
    }

    public void visit(FunctionCallExpressionNode functionCallNode) {
        visitSyntaxNode(functionCallNode);
    }

    public void visit(BracedExpression bracedExpression) {
        visitSyntaxNode(bracedExpression);
    }

    public void visit(TypeofExpression typeofExpression) {
        visitSyntaxNode(typeofExpression);
    }

    // Tokens

    public void visit(Token token) {
    }

    // Misc

    public void visit(EmptyNode emptyNode) {
    }

    public void visit(Minutiae minutiae) {
    }

    public void visit(RequiredParameter requiredParameter) {
        visitSyntaxNode(requiredParameter);
    }

    public void visit(PositionalArgumentNode positionalArgumentNode) {
        visitSyntaxNode(positionalArgumentNode);
    }

    public void visit(NamedArgumentNode namedArgumentNode) {
        visitSyntaxNode(namedArgumentNode);
    }

    public void visit(RestArgumentNode restArgumentNode) {
        visitSyntaxNode(restArgumentNode);
    }

    public void visit(ObjectFieldNode objectFieldNode) {
        visitSyntaxNode(objectFieldNode);
    }

    public void visit(RecordFieldNode recordFieldNode) {
        visitSyntaxNode(recordFieldNode);
    }

    public void visit(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        visitSyntaxNode(recordFieldWithDefaultValueNode);
    }

    public void visit(RecordRestDescriptorNode recordRestDescriptorNode) {
        visitSyntaxNode(recordRestDescriptorNode);
    }

//    public void visit(NodeList nodeList) {
//        visitSyntaxNode(nodeList);
//    }

    public void visit(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        visitSyntaxNode(recordTypeDescriptorNode);
    }

    public void visit(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        visitSyntaxNode(objectTypeDescriptorNode);
    }

    public void visit(TypeReferenceNode typeReferenceNode) {
        visitSyntaxNode(typeReferenceNode);
    }

    public void visit(ImportPrefix importPrefix) {
        visitSyntaxNode(importPrefix);
    }

    public void visit(ImportVersion importVersion) {
        visitSyntaxNode(importVersion);
    }

    public void visit(ImportSubVersion importSubVersion) {
        visitSyntaxNode(importSubVersion);
    }

    public void visit(SubModuleName subModuleName) {
        visitSyntaxNode(subModuleName);
    }

    public void visit(ImportOrgName importOrgName) {
        visitSyntaxNode(importOrgName);
    }

    public void visit(ComputedNameField computedNameField) {
        visitSyntaxNode(computedNameField);
    }

    public void visit(MappingConstructorExpression mappingConstructorExpr) {
        visitSyntaxNode(mappingConstructorExpr);
    }

    public void visit(SpecificField specificField) {
        visitSyntaxNode(specificField);
    }

    public void visit(SpreadField spreadField) {
        visitSyntaxNode(spreadField);
    }

    public void visit(ServiceBody serviceBody) {
        visitSyntaxNode(serviceBody);
    }

    public void visit(ServiceDeclarationNode serviceDecl) {
        visitSyntaxNode(serviceDecl);
    }

    public void visit(ExpressionListItem expressionListItem) {
        visitSyntaxNode(expressionListItem);
    }

    public void visit(ListenerDeclaration listenerDecl) {
        visitSyntaxNode(listenerDecl);
    }

    public void visit(ConstantDeclaration constantDecl) {
        visitSyntaxNode(constantDecl);
    }

    public void visit(NilTypeDescriptor nilTypeDescriptor) {
        visitSyntaxNode(nilTypeDescriptor);
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
