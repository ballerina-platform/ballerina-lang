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

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;

import java.util.Objects;

/**
 * A factory for creating nodes in the syntax tree.
 *
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class NodeFactory extends AbstractNodeFactory {

    private NodeFactory() {
    }

    public static ModulePartNode createModulePartNode(
            NodeList<ImportDeclarationNode> imports,
            NodeList<ModuleMemberDeclarationNode> members,
            Token eofToken) {
        Objects.requireNonNull(imports, "imports must not be null");
        Objects.requireNonNull(members, "members must not be null");
        Objects.requireNonNull(eofToken, "eofToken must not be null");

        STNode stModulePartNode = STNodeFactory.createModulePartNode(
                imports.underlyingListNode().internalNode(),
                members.underlyingListNode().internalNode(),
                eofToken.internalNode());
        return stModulePartNode.createUnlinkedFacade();
    }

    public static FunctionDefinitionNode createFunctionDefinitionNode(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token functionKeyword,
            IdentifierToken functionName,
            FunctionSignatureNode functionSignature,
            FunctionBodyNode functionBody) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
        Objects.requireNonNull(functionName, "functionName must not be null");
        Objects.requireNonNull(functionSignature, "functionSignature must not be null");
        Objects.requireNonNull(functionBody, "functionBody must not be null");

        STNode stFunctionDefinitionNode = STNodeFactory.createFunctionDefinitionNode(
                metadata.internalNode(),
                getOptionalSTNode(visibilityQualifier),
                functionKeyword.internalNode(),
                functionName.internalNode(),
                functionSignature.internalNode(),
                functionBody.internalNode());
        return stFunctionDefinitionNode.createUnlinkedFacade();
    }

    public static ImportDeclarationNode createImportDeclarationNode(
            Token importKeyword,
            ImportOrgNameNode orgName,
            SeparatedNodeList<IdentifierToken> moduleName,
            ImportVersionNode version,
            ImportPrefixNode prefix,
            Token semicolon) {
        Objects.requireNonNull(importKeyword, "importKeyword must not be null");
        Objects.requireNonNull(moduleName, "moduleName must not be null");
        Objects.requireNonNull(semicolon, "semicolon must not be null");

        STNode stImportDeclarationNode = STNodeFactory.createImportDeclarationNode(
                importKeyword.internalNode(),
                getOptionalSTNode(orgName),
                moduleName.underlyingListNode().internalNode(),
                getOptionalSTNode(version),
                getOptionalSTNode(prefix),
                semicolon.internalNode());
        return stImportDeclarationNode.createUnlinkedFacade();
    }

    public static ListenerDeclarationNode createListenerDeclarationNode(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token listenerKeyword,
            Node typeDescriptor,
            Token variableName,
            Token equalsToken,
            Node initializer,
            Token semicolonToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(listenerKeyword, "listenerKeyword must not be null");
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(variableName, "variableName must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(initializer, "initializer must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stListenerDeclarationNode = STNodeFactory.createListenerDeclarationNode(
                metadata.internalNode(),
                getOptionalSTNode(visibilityQualifier),
                listenerKeyword.internalNode(),
                typeDescriptor.internalNode(),
                variableName.internalNode(),
                equalsToken.internalNode(),
                initializer.internalNode(),
                semicolonToken.internalNode());
        return stListenerDeclarationNode.createUnlinkedFacade();
    }

    public static TypeDefinitionNode createTypeDefinitionNode(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token typeKeyword,
            Token typeName,
            Node typeDescriptor,
            Token semicolonToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(typeKeyword, "typeKeyword must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stTypeDefinitionNode = STNodeFactory.createTypeDefinitionNode(
                metadata.internalNode(),
                getOptionalSTNode(visibilityQualifier),
                typeKeyword.internalNode(),
                typeName.internalNode(),
                typeDescriptor.internalNode(),
                semicolonToken.internalNode());
        return stTypeDefinitionNode.createUnlinkedFacade();
    }

    public static ServiceDeclarationNode createServiceDeclarationNode(
            MetadataNode metadata,
            Token serviceKeyword,
            IdentifierToken serviceName,
            Token onKeyword,
            NodeList<ExpressionNode> expressions,
            Node serviceBody) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(serviceKeyword, "serviceKeyword must not be null");
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        Objects.requireNonNull(onKeyword, "onKeyword must not be null");
        Objects.requireNonNull(expressions, "expressions must not be null");
        Objects.requireNonNull(serviceBody, "serviceBody must not be null");

        STNode stServiceDeclarationNode = STNodeFactory.createServiceDeclarationNode(
                metadata.internalNode(),
                serviceKeyword.internalNode(),
                serviceName.internalNode(),
                onKeyword.internalNode(),
                expressions.underlyingListNode().internalNode(),
                serviceBody.internalNode());
        return stServiceDeclarationNode.createUnlinkedFacade();
    }

    public static AssignmentStatementNode createAssignmentStatementNode(
            Node varRef,
            Token equalsToken,
            ExpressionNode expression,
            Token semicolonToken) {
        Objects.requireNonNull(varRef, "varRef must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stAssignmentStatementNode = STNodeFactory.createAssignmentStatementNode(
                varRef.internalNode(),
                equalsToken.internalNode(),
                expression.internalNode(),
                semicolonToken.internalNode());
        return stAssignmentStatementNode.createUnlinkedFacade();
    }

    public static CompoundAssignmentStatementNode createCompoundAssignmentStatementNode(
            ExpressionNode lhsExpression,
            Token binaryOperator,
            Token equalsToken,
            ExpressionNode rhsExpression,
            Token semicolonToken) {
        Objects.requireNonNull(lhsExpression, "lhsExpression must not be null");
        Objects.requireNonNull(binaryOperator, "binaryOperator must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(rhsExpression, "rhsExpression must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stCompoundAssignmentStatementNode = STNodeFactory.createCompoundAssignmentStatementNode(
                lhsExpression.internalNode(),
                binaryOperator.internalNode(),
                equalsToken.internalNode(),
                rhsExpression.internalNode(),
                semicolonToken.internalNode());
        return stCompoundAssignmentStatementNode.createUnlinkedFacade();
    }

    public static VariableDeclarationNode createVariableDeclarationNode(
            NodeList<AnnotationNode> annotations,
            Token finalKeyword,
            TypedBindingPatternNode typedBindingPattern,
            Token equalsToken,
            ExpressionNode initializer,
            Token semicolonToken) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stVariableDeclarationNode = STNodeFactory.createVariableDeclarationNode(
                annotations.underlyingListNode().internalNode(),
                getOptionalSTNode(finalKeyword),
                typedBindingPattern.internalNode(),
                getOptionalSTNode(equalsToken),
                getOptionalSTNode(initializer),
                semicolonToken.internalNode());
        return stVariableDeclarationNode.createUnlinkedFacade();
    }

    public static BlockStatementNode createBlockStatementNode(
            Token openBraceToken,
            NodeList<StatementNode> statements,
            Token closeBraceToken) {
        Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
        Objects.requireNonNull(statements, "statements must not be null");
        Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");

        STNode stBlockStatementNode = STNodeFactory.createBlockStatementNode(
                openBraceToken.internalNode(),
                statements.underlyingListNode().internalNode(),
                closeBraceToken.internalNode());
        return stBlockStatementNode.createUnlinkedFacade();
    }

    public static BreakStatementNode createBreakStatementNode(
            Token breakToken,
            Token semicolonToken) {
        Objects.requireNonNull(breakToken, "breakToken must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stBreakStatementNode = STNodeFactory.createBreakStatementNode(
                breakToken.internalNode(),
                semicolonToken.internalNode());
        return stBreakStatementNode.createUnlinkedFacade();
    }

    public static ExpressionStatementNode createExpressionStatementNode(
            SyntaxKind kind,
            ExpressionNode expression,
            Token semicolonToken) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stExpressionStatementNode = STNodeFactory.createExpressionStatementNode(
                kind,
                expression.internalNode(),
                semicolonToken.internalNode());
        return stExpressionStatementNode.createUnlinkedFacade();
    }

    public static ContinueStatementNode createContinueStatementNode(
            Token continueToken,
            Token semicolonToken) {
        Objects.requireNonNull(continueToken, "continueToken must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stContinueStatementNode = STNodeFactory.createContinueStatementNode(
                continueToken.internalNode(),
                semicolonToken.internalNode());
        return stContinueStatementNode.createUnlinkedFacade();
    }

    public static ExternalFunctionBodyNode createExternalFunctionBodyNode(
            Token equalsToken,
            NodeList<AnnotationNode> annotations,
            Token externalKeyword,
            Token semicolonToken) {
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(externalKeyword, "externalKeyword must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stExternalFunctionBodyNode = STNodeFactory.createExternalFunctionBodyNode(
                equalsToken.internalNode(),
                annotations.underlyingListNode().internalNode(),
                externalKeyword.internalNode(),
                semicolonToken.internalNode());
        return stExternalFunctionBodyNode.createUnlinkedFacade();
    }

    public static IfElseStatementNode createIfElseStatementNode(
            Token ifKeyword,
            ExpressionNode condition,
            BlockStatementNode ifBody,
            Node elseBody) {
        Objects.requireNonNull(ifKeyword, "ifKeyword must not be null");
        Objects.requireNonNull(condition, "condition must not be null");
        Objects.requireNonNull(ifBody, "ifBody must not be null");

        STNode stIfElseStatementNode = STNodeFactory.createIfElseStatementNode(
                ifKeyword.internalNode(),
                condition.internalNode(),
                ifBody.internalNode(),
                getOptionalSTNode(elseBody));
        return stIfElseStatementNode.createUnlinkedFacade();
    }

    public static ElseBlockNode createElseBlockNode(
            Token elseKeyword,
            StatementNode elseBody) {
        Objects.requireNonNull(elseKeyword, "elseKeyword must not be null");
        Objects.requireNonNull(elseBody, "elseBody must not be null");

        STNode stElseBlockNode = STNodeFactory.createElseBlockNode(
                elseKeyword.internalNode(),
                elseBody.internalNode());
        return stElseBlockNode.createUnlinkedFacade();
    }

    public static WhileStatementNode createWhileStatementNode(
            Token whileKeyword,
            ExpressionNode condition,
            BlockStatementNode whileBody) {
        Objects.requireNonNull(whileKeyword, "whileKeyword must not be null");
        Objects.requireNonNull(condition, "condition must not be null");
        Objects.requireNonNull(whileBody, "whileBody must not be null");

        STNode stWhileStatementNode = STNodeFactory.createWhileStatementNode(
                whileKeyword.internalNode(),
                condition.internalNode(),
                whileBody.internalNode());
        return stWhileStatementNode.createUnlinkedFacade();
    }

    public static PanicStatementNode createPanicStatementNode(
            Token panicKeyword,
            ExpressionNode expression,
            Token semicolonToken) {
        Objects.requireNonNull(panicKeyword, "panicKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stPanicStatementNode = STNodeFactory.createPanicStatementNode(
                panicKeyword.internalNode(),
                expression.internalNode(),
                semicolonToken.internalNode());
        return stPanicStatementNode.createUnlinkedFacade();
    }

    public static ReturnStatementNode createReturnStatementNode(
            Token returnKeyword,
            ExpressionNode expression,
            Token semicolonToken) {
        Objects.requireNonNull(returnKeyword, "returnKeyword must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stReturnStatementNode = STNodeFactory.createReturnStatementNode(
                returnKeyword.internalNode(),
                getOptionalSTNode(expression),
                semicolonToken.internalNode());
        return stReturnStatementNode.createUnlinkedFacade();
    }

    public static LocalTypeDefinitionStatementNode createLocalTypeDefinitionStatementNode(
            NodeList<AnnotationNode> annotations,
            Token typeKeyword,
            Node typeName,
            Node typeDescriptor,
            Token semicolonToken) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(typeKeyword, "typeKeyword must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stLocalTypeDefinitionStatementNode = STNodeFactory.createLocalTypeDefinitionStatementNode(
                annotations.underlyingListNode().internalNode(),
                typeKeyword.internalNode(),
                typeName.internalNode(),
                typeDescriptor.internalNode(),
                semicolonToken.internalNode());
        return stLocalTypeDefinitionStatementNode.createUnlinkedFacade();
    }

    public static LockStatementNode createLockStatementNode(
            Token lockKeyword,
            StatementNode blockStatement) {
        Objects.requireNonNull(lockKeyword, "lockKeyword must not be null");
        Objects.requireNonNull(blockStatement, "blockStatement must not be null");

        STNode stLockStatementNode = STNodeFactory.createLockStatementNode(
                lockKeyword.internalNode(),
                blockStatement.internalNode());
        return stLockStatementNode.createUnlinkedFacade();
    }

    public static ForkStatementNode createForkStatementNode(
            Token forkKeyword,
            Token openBraceToken,
            NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations,
            Token closeBraceToken) {
        Objects.requireNonNull(forkKeyword, "forkKeyword must not be null");
        Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
        Objects.requireNonNull(namedWorkerDeclarations, "namedWorkerDeclarations must not be null");
        Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");

        STNode stForkStatementNode = STNodeFactory.createForkStatementNode(
                forkKeyword.internalNode(),
                openBraceToken.internalNode(),
                namedWorkerDeclarations.underlyingListNode().internalNode(),
                closeBraceToken.internalNode());
        return stForkStatementNode.createUnlinkedFacade();
    }

    public static ForEachStatementNode createForEachStatementNode(
            Token forEachKeyword,
            TypedBindingPatternNode typedBindingPattern,
            Token inKeyword,
            Node actionOrExpressionNode,
            StatementNode blockStatement) {
        Objects.requireNonNull(forEachKeyword, "forEachKeyword must not be null");
        Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
        Objects.requireNonNull(inKeyword, "inKeyword must not be null");
        Objects.requireNonNull(actionOrExpressionNode, "actionOrExpressionNode must not be null");
        Objects.requireNonNull(blockStatement, "blockStatement must not be null");

        STNode stForEachStatementNode = STNodeFactory.createForEachStatementNode(
                forEachKeyword.internalNode(),
                typedBindingPattern.internalNode(),
                inKeyword.internalNode(),
                actionOrExpressionNode.internalNode(),
                blockStatement.internalNode());
        return stForEachStatementNode.createUnlinkedFacade();
    }

    public static BinaryExpressionNode createBinaryExpressionNode(
            SyntaxKind kind,
            Node lhsExpr,
            Token operator,
            Node rhsExpr) {
        Objects.requireNonNull(lhsExpr, "lhsExpr must not be null");
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(rhsExpr, "rhsExpr must not be null");

        STNode stBinaryExpressionNode = STNodeFactory.createBinaryExpressionNode(
                kind,
                lhsExpr.internalNode(),
                operator.internalNode(),
                rhsExpr.internalNode());
        return stBinaryExpressionNode.createUnlinkedFacade();
    }

    public static BracedExpressionNode createBracedExpressionNode(
            SyntaxKind kind,
            Token openParen,
            ExpressionNode expression,
            Token closeParen) {
        Objects.requireNonNull(openParen, "openParen must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(closeParen, "closeParen must not be null");

        STNode stBracedExpressionNode = STNodeFactory.createBracedExpressionNode(
                kind,
                openParen.internalNode(),
                expression.internalNode(),
                closeParen.internalNode());
        return stBracedExpressionNode.createUnlinkedFacade();
    }

    public static CheckExpressionNode createCheckExpressionNode(
            SyntaxKind kind,
            Token checkKeyword,
            ExpressionNode expression) {
        Objects.requireNonNull(checkKeyword, "checkKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stCheckExpressionNode = STNodeFactory.createCheckExpressionNode(
                kind,
                checkKeyword.internalNode(),
                expression.internalNode());
        return stCheckExpressionNode.createUnlinkedFacade();
    }

    public static FieldAccessExpressionNode createFieldAccessExpressionNode(
            ExpressionNode expression,
            Token dotToken,
            NameReferenceNode fieldName) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(dotToken, "dotToken must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");

        STNode stFieldAccessExpressionNode = STNodeFactory.createFieldAccessExpressionNode(
                expression.internalNode(),
                dotToken.internalNode(),
                fieldName.internalNode());
        return stFieldAccessExpressionNode.createUnlinkedFacade();
    }

    public static FunctionCallExpressionNode createFunctionCallExpressionNode(
            Node functionName,
            Token openParenToken,
            NodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        Objects.requireNonNull(functionName, "functionName must not be null");
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stFunctionCallExpressionNode = STNodeFactory.createFunctionCallExpressionNode(
                functionName.internalNode(),
                openParenToken.internalNode(),
                arguments.underlyingListNode().internalNode(),
                closeParenToken.internalNode());
        return stFunctionCallExpressionNode.createUnlinkedFacade();
    }

    public static MethodCallExpressionNode createMethodCallExpressionNode(
            ExpressionNode expression,
            Token dotToken,
            NameReferenceNode methodName,
            Token openParenToken,
            NodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(dotToken, "dotToken must not be null");
        Objects.requireNonNull(methodName, "methodName must not be null");
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stMethodCallExpressionNode = STNodeFactory.createMethodCallExpressionNode(
                expression.internalNode(),
                dotToken.internalNode(),
                methodName.internalNode(),
                openParenToken.internalNode(),
                arguments.underlyingListNode().internalNode(),
                closeParenToken.internalNode());
        return stMethodCallExpressionNode.createUnlinkedFacade();
    }

    public static MappingConstructorExpressionNode createMappingConstructorExpressionNode(
            Token openBrace,
            SeparatedNodeList<MappingFieldNode> fields,
            Token closeBrace) {
        Objects.requireNonNull(openBrace, "openBrace must not be null");
        Objects.requireNonNull(fields, "fields must not be null");
        Objects.requireNonNull(closeBrace, "closeBrace must not be null");

        STNode stMappingConstructorExpressionNode = STNodeFactory.createMappingConstructorExpressionNode(
                openBrace.internalNode(),
                fields.underlyingListNode().internalNode(),
                closeBrace.internalNode());
        return stMappingConstructorExpressionNode.createUnlinkedFacade();
    }

    public static IndexedExpressionNode createIndexedExpressionNode(
            ExpressionNode containerExpression,
            Token openBracket,
            SeparatedNodeList<ExpressionNode> keyExpression,
            Token closeBracket) {
        Objects.requireNonNull(containerExpression, "containerExpression must not be null");
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(keyExpression, "keyExpression must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");

        STNode stIndexedExpressionNode = STNodeFactory.createIndexedExpressionNode(
                containerExpression.internalNode(),
                openBracket.internalNode(),
                keyExpression.underlyingListNode().internalNode(),
                closeBracket.internalNode());
        return stIndexedExpressionNode.createUnlinkedFacade();
    }

    public static TypeofExpressionNode createTypeofExpressionNode(
            Token typeofKeyword,
            ExpressionNode expression) {
        Objects.requireNonNull(typeofKeyword, "typeofKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stTypeofExpressionNode = STNodeFactory.createTypeofExpressionNode(
                typeofKeyword.internalNode(),
                expression.internalNode());
        return stTypeofExpressionNode.createUnlinkedFacade();
    }

    public static UnaryExpressionNode createUnaryExpressionNode(
            Token unaryOperator,
            ExpressionNode expression) {
        Objects.requireNonNull(unaryOperator, "unaryOperator must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stUnaryExpressionNode = STNodeFactory.createUnaryExpressionNode(
                unaryOperator.internalNode(),
                expression.internalNode());
        return stUnaryExpressionNode.createUnlinkedFacade();
    }

    public static ComputedNameFieldNode createComputedNameFieldNode(
            Token openBracket,
            ExpressionNode fieldNameExpr,
            Token closeBracket,
            Token colonToken,
            ExpressionNode valueExpr) {
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(fieldNameExpr, "fieldNameExpr must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");
        Objects.requireNonNull(colonToken, "colonToken must not be null");
        Objects.requireNonNull(valueExpr, "valueExpr must not be null");

        STNode stComputedNameFieldNode = STNodeFactory.createComputedNameFieldNode(
                openBracket.internalNode(),
                fieldNameExpr.internalNode(),
                closeBracket.internalNode(),
                colonToken.internalNode(),
                valueExpr.internalNode());
        return stComputedNameFieldNode.createUnlinkedFacade();
    }

    public static ConstantDeclarationNode createConstantDeclarationNode(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token constKeyword,
            TypeDescriptorNode typeDescriptor,
            Token variableName,
            Token equalsToken,
            Node initializer,
            Token semicolonToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
        Objects.requireNonNull(constKeyword, "constKeyword must not be null");
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(variableName, "variableName must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(initializer, "initializer must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stConstantDeclarationNode = STNodeFactory.createConstantDeclarationNode(
                metadata.internalNode(),
                visibilityQualifier.internalNode(),
                constKeyword.internalNode(),
                typeDescriptor.internalNode(),
                variableName.internalNode(),
                equalsToken.internalNode(),
                initializer.internalNode(),
                semicolonToken.internalNode());
        return stConstantDeclarationNode.createUnlinkedFacade();
    }

    public static DefaultableParameterNode createDefaultableParameterNode(
            Token leadingComma,
            NodeList<AnnotationNode> annotations,
            Token visibilityQualifier,
            Node typeName,
            Token paramName,
            Token equalsToken,
            Node expression) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stDefaultableParameterNode = STNodeFactory.createDefaultableParameterNode(
                getOptionalSTNode(leadingComma),
                annotations.underlyingListNode().internalNode(),
                getOptionalSTNode(visibilityQualifier),
                typeName.internalNode(),
                getOptionalSTNode(paramName),
                equalsToken.internalNode(),
                expression.internalNode());
        return stDefaultableParameterNode.createUnlinkedFacade();
    }

    public static RequiredParameterNode createRequiredParameterNode(
            Token leadingComma,
            NodeList<AnnotationNode> annotations,
            Token visibilityQualifier,
            Node typeName,
            Token paramName) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");

        STNode stRequiredParameterNode = STNodeFactory.createRequiredParameterNode(
                getOptionalSTNode(leadingComma),
                annotations.underlyingListNode().internalNode(),
                getOptionalSTNode(visibilityQualifier),
                typeName.internalNode(),
                getOptionalSTNode(paramName));
        return stRequiredParameterNode.createUnlinkedFacade();
    }

    public static RestParameterNode createRestParameterNode(
            Token leadingComma,
            NodeList<AnnotationNode> annotations,
            Node typeName,
            Token ellipsisToken,
            Token paramName) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");

        STNode stRestParameterNode = STNodeFactory.createRestParameterNode(
                getOptionalSTNode(leadingComma),
                annotations.underlyingListNode().internalNode(),
                typeName.internalNode(),
                ellipsisToken.internalNode(),
                getOptionalSTNode(paramName));
        return stRestParameterNode.createUnlinkedFacade();
    }

    public static ExpressionListItemNode createExpressionListItemNode(
            Token leadingComma,
            ExpressionNode expression) {
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stExpressionListItemNode = STNodeFactory.createExpressionListItemNode(
                getOptionalSTNode(leadingComma),
                expression.internalNode());
        return stExpressionListItemNode.createUnlinkedFacade();
    }

    public static ImportOrgNameNode createImportOrgNameNode(
            Token orgName,
            Token slashToken) {
        Objects.requireNonNull(orgName, "orgName must not be null");
        Objects.requireNonNull(slashToken, "slashToken must not be null");

        STNode stImportOrgNameNode = STNodeFactory.createImportOrgNameNode(
                orgName.internalNode(),
                slashToken.internalNode());
        return stImportOrgNameNode.createUnlinkedFacade();
    }

    public static ImportPrefixNode createImportPrefixNode(
            Token asKeyword,
            Token prefix) {
        Objects.requireNonNull(asKeyword, "asKeyword must not be null");
        Objects.requireNonNull(prefix, "prefix must not be null");

        STNode stImportPrefixNode = STNodeFactory.createImportPrefixNode(
                asKeyword.internalNode(),
                prefix.internalNode());
        return stImportPrefixNode.createUnlinkedFacade();
    }

    public static ImportSubVersionNode createImportSubVersionNode(
            Token leadingDot,
            Token versionNumber) {
        Objects.requireNonNull(versionNumber, "versionNumber must not be null");

        STNode stImportSubVersionNode = STNodeFactory.createImportSubVersionNode(
                getOptionalSTNode(leadingDot),
                versionNumber.internalNode());
        return stImportSubVersionNode.createUnlinkedFacade();
    }

    public static ImportVersionNode createImportVersionNode(
            Token versionKeyword,
            Node versionNumber) {
        Objects.requireNonNull(versionKeyword, "versionKeyword must not be null");
        Objects.requireNonNull(versionNumber, "versionNumber must not be null");

        STNode stImportVersionNode = STNodeFactory.createImportVersionNode(
                versionKeyword.internalNode(),
                versionNumber.internalNode());
        return stImportVersionNode.createUnlinkedFacade();
    }

    public static SpecificFieldNode createSpecificFieldNode(
            Token readonlyKeyword,
            Token fieldName,
            Token colon,
            ExpressionNode valueExpr) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(colon, "colon must not be null");
        Objects.requireNonNull(valueExpr, "valueExpr must not be null");

        STNode stSpecificFieldNode = STNodeFactory.createSpecificFieldNode(
                getOptionalSTNode(readonlyKeyword),
                fieldName.internalNode(),
                colon.internalNode(),
                valueExpr.internalNode());
        return stSpecificFieldNode.createUnlinkedFacade();
    }

    public static SpreadFieldNode createSpreadFieldNode(
            Token ellipsis,
            ExpressionNode valueExpr) {
        Objects.requireNonNull(ellipsis, "ellipsis must not be null");
        Objects.requireNonNull(valueExpr, "valueExpr must not be null");

        STNode stSpreadFieldNode = STNodeFactory.createSpreadFieldNode(
                ellipsis.internalNode(),
                valueExpr.internalNode());
        return stSpreadFieldNode.createUnlinkedFacade();
    }

    public static NamedArgumentNode createNamedArgumentNode(
            Token leadingComma,
            SimpleNameReferenceNode argumentName,
            Token equalsToken,
            ExpressionNode expression) {
        Objects.requireNonNull(argumentName, "argumentName must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stNamedArgumentNode = STNodeFactory.createNamedArgumentNode(
                getOptionalSTNode(leadingComma),
                argumentName.internalNode(),
                equalsToken.internalNode(),
                expression.internalNode());
        return stNamedArgumentNode.createUnlinkedFacade();
    }

    public static PositionalArgumentNode createPositionalArgumentNode(
            Token leadingComma,
            ExpressionNode expression) {
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stPositionalArgumentNode = STNodeFactory.createPositionalArgumentNode(
                getOptionalSTNode(leadingComma),
                expression.internalNode());
        return stPositionalArgumentNode.createUnlinkedFacade();
    }

    public static RestArgumentNode createRestArgumentNode(
            Token leadingComma,
            Token ellipsis,
            ExpressionNode expression) {
        Objects.requireNonNull(ellipsis, "ellipsis must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stRestArgumentNode = STNodeFactory.createRestArgumentNode(
                getOptionalSTNode(leadingComma),
                ellipsis.internalNode(),
                expression.internalNode());
        return stRestArgumentNode.createUnlinkedFacade();
    }

    public static ObjectTypeDescriptorNode createObjectTypeDescriptorNode(
            NodeList<Token> objectTypeQualifiers,
            Token objectKeyword,
            Token openBrace,
            NodeList<Node> members,
            Token closeBrace) {
        Objects.requireNonNull(objectTypeQualifiers, "objectTypeQualifiers must not be null");
        Objects.requireNonNull(objectKeyword, "objectKeyword must not be null");
        Objects.requireNonNull(openBrace, "openBrace must not be null");
        Objects.requireNonNull(members, "members must not be null");
        Objects.requireNonNull(closeBrace, "closeBrace must not be null");

        STNode stObjectTypeDescriptorNode = STNodeFactory.createObjectTypeDescriptorNode(
                objectTypeQualifiers.underlyingListNode().internalNode(),
                objectKeyword.internalNode(),
                openBrace.internalNode(),
                members.underlyingListNode().internalNode(),
                closeBrace.internalNode());
        return stObjectTypeDescriptorNode.createUnlinkedFacade();
    }

    public static RecordTypeDescriptorNode createRecordTypeDescriptorNode(
            Token objectKeyword,
            Token bodyStartDelimiter,
            NodeList<Node> fields,
            Token bodyEndDelimiter) {
        Objects.requireNonNull(objectKeyword, "objectKeyword must not be null");
        Objects.requireNonNull(bodyStartDelimiter, "bodyStartDelimiter must not be null");
        Objects.requireNonNull(fields, "fields must not be null");
        Objects.requireNonNull(bodyEndDelimiter, "bodyEndDelimiter must not be null");

        STNode stRecordTypeDescriptorNode = STNodeFactory.createRecordTypeDescriptorNode(
                objectKeyword.internalNode(),
                bodyStartDelimiter.internalNode(),
                fields.underlyingListNode().internalNode(),
                bodyEndDelimiter.internalNode());
        return stRecordTypeDescriptorNode.createUnlinkedFacade();
    }

    public static ReturnTypeDescriptorNode createReturnTypeDescriptorNode(
            Token returnsKeyword,
            NodeList<AnnotationNode> annotations,
            Node type) {
        Objects.requireNonNull(returnsKeyword, "returnsKeyword must not be null");
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(type, "type must not be null");

        STNode stReturnTypeDescriptorNode = STNodeFactory.createReturnTypeDescriptorNode(
                returnsKeyword.internalNode(),
                annotations.underlyingListNode().internalNode(),
                type.internalNode());
        return stReturnTypeDescriptorNode.createUnlinkedFacade();
    }

    public static NilTypeDescriptorNode createNilTypeDescriptorNode(
            Token openParenToken,
            Token closeParenToken) {
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stNilTypeDescriptorNode = STNodeFactory.createNilTypeDescriptorNode(
                openParenToken.internalNode(),
                closeParenToken.internalNode());
        return stNilTypeDescriptorNode.createUnlinkedFacade();
    }

    public static OptionalTypeDescriptorNode createOptionalTypeDescriptorNode(
            Node typeDescriptor,
            Token questionMarkToken) {
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(questionMarkToken, "questionMarkToken must not be null");

        STNode stOptionalTypeDescriptorNode = STNodeFactory.createOptionalTypeDescriptorNode(
                typeDescriptor.internalNode(),
                questionMarkToken.internalNode());
        return stOptionalTypeDescriptorNode.createUnlinkedFacade();
    }

    public static ObjectFieldNode createObjectFieldNode(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token readonlyKeyword,
            Node typeName,
            Token fieldName,
            Token equalsToken,
            ExpressionNode expression,
            Token semicolonToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stObjectFieldNode = STNodeFactory.createObjectFieldNode(
                metadata.internalNode(),
                getOptionalSTNode(visibilityQualifier),
                getOptionalSTNode(readonlyKeyword),
                typeName.internalNode(),
                fieldName.internalNode(),
                equalsToken.internalNode(),
                expression.internalNode(),
                semicolonToken.internalNode());
        return stObjectFieldNode.createUnlinkedFacade();
    }

    public static RecordFieldNode createRecordFieldNode(
            MetadataNode metadata,
            Token readonlyKeyword,
            Node typeName,
            Token fieldName,
            Token questionMarkToken,
            Token semicolonToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stRecordFieldNode = STNodeFactory.createRecordFieldNode(
                metadata.internalNode(),
                getOptionalSTNode(readonlyKeyword),
                typeName.internalNode(),
                fieldName.internalNode(),
                getOptionalSTNode(questionMarkToken),
                semicolonToken.internalNode());
        return stRecordFieldNode.createUnlinkedFacade();
    }

    public static RecordFieldWithDefaultValueNode createRecordFieldWithDefaultValueNode(
            MetadataNode metadata,
            Token readonlyKeyword,
            Node typeName,
            Token fieldName,
            Token equalsToken,
            ExpressionNode expression,
            Token semicolonToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stRecordFieldWithDefaultValueNode = STNodeFactory.createRecordFieldWithDefaultValueNode(
                metadata.internalNode(),
                getOptionalSTNode(readonlyKeyword),
                typeName.internalNode(),
                fieldName.internalNode(),
                equalsToken.internalNode(),
                expression.internalNode(),
                semicolonToken.internalNode());
        return stRecordFieldWithDefaultValueNode.createUnlinkedFacade();
    }

    public static RecordRestDescriptorNode createRecordRestDescriptorNode(
            Node typeName,
            Token ellipsisToken,
            Token semicolonToken) {
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stRecordRestDescriptorNode = STNodeFactory.createRecordRestDescriptorNode(
                typeName.internalNode(),
                ellipsisToken.internalNode(),
                semicolonToken.internalNode());
        return stRecordRestDescriptorNode.createUnlinkedFacade();
    }

    public static TypeReferenceNode createTypeReferenceNode(
            Token asteriskToken,
            Node typeName,
            Token semicolonToken) {
        Objects.requireNonNull(asteriskToken, "asteriskToken must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stTypeReferenceNode = STNodeFactory.createTypeReferenceNode(
                asteriskToken.internalNode(),
                typeName.internalNode(),
                semicolonToken.internalNode());
        return stTypeReferenceNode.createUnlinkedFacade();
    }

    public static ServiceBodyNode createServiceBodyNode(
            Token openBraceToken,
            NodeList<Node> resources,
            Token closeBraceToken) {
        Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
        Objects.requireNonNull(resources, "resources must not be null");
        Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");

        STNode stServiceBodyNode = STNodeFactory.createServiceBodyNode(
                openBraceToken.internalNode(),
                resources.underlyingListNode().internalNode(),
                closeBraceToken.internalNode());
        return stServiceBodyNode.createUnlinkedFacade();
    }

    public static AnnotationNode createAnnotationNode(
            Token atToken,
            Node annotReference,
            MappingConstructorExpressionNode annotValue) {
        Objects.requireNonNull(atToken, "atToken must not be null");
        Objects.requireNonNull(annotReference, "annotReference must not be null");

        STNode stAnnotationNode = STNodeFactory.createAnnotationNode(
                atToken.internalNode(),
                annotReference.internalNode(),
                getOptionalSTNode(annotValue));
        return stAnnotationNode.createUnlinkedFacade();
    }

    public static MetadataNode createMetadataNode(
            Node documentationString,
            NodeList<AnnotationNode> annotations) {
        Objects.requireNonNull(annotations, "annotations must not be null");

        STNode stMetadataNode = STNodeFactory.createMetadataNode(
                getOptionalSTNode(documentationString),
                annotations.underlyingListNode().internalNode());
        return stMetadataNode.createUnlinkedFacade();
    }

    public static ModuleVariableDeclarationNode createModuleVariableDeclarationNode(
            MetadataNode metadata,
            Token finalKeyword,
            TypedBindingPatternNode typedBindingPattern,
            Token equalsToken,
            ExpressionNode initializer,
            Token semicolonToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(initializer, "initializer must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stModuleVariableDeclarationNode = STNodeFactory.createModuleVariableDeclarationNode(
                metadata.internalNode(),
                getOptionalSTNode(finalKeyword),
                typedBindingPattern.internalNode(),
                equalsToken.internalNode(),
                initializer.internalNode(),
                semicolonToken.internalNode());
        return stModuleVariableDeclarationNode.createUnlinkedFacade();
    }

    public static TypeTestExpressionNode createTypeTestExpressionNode(
            ExpressionNode expression,
            Token isKeyword,
            Node typeDescriptor) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(isKeyword, "isKeyword must not be null");
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");

        STNode stTypeTestExpressionNode = STNodeFactory.createTypeTestExpressionNode(
                expression.internalNode(),
                isKeyword.internalNode(),
                typeDescriptor.internalNode());
        return stTypeTestExpressionNode.createUnlinkedFacade();
    }

    public static RemoteMethodCallActionNode createRemoteMethodCallActionNode(
            ExpressionNode expression,
            Token rightArrowToken,
            SimpleNameReferenceNode methodName,
            Token openParenToken,
            NodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(rightArrowToken, "rightArrowToken must not be null");
        Objects.requireNonNull(methodName, "methodName must not be null");
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stRemoteMethodCallActionNode = STNodeFactory.createRemoteMethodCallActionNode(
                expression.internalNode(),
                rightArrowToken.internalNode(),
                methodName.internalNode(),
                openParenToken.internalNode(),
                arguments.underlyingListNode().internalNode(),
                closeParenToken.internalNode());
        return stRemoteMethodCallActionNode.createUnlinkedFacade();
    }

    public static ParameterizedTypeDescriptorNode createParameterizedTypeDescriptorNode(
            Token parameterizedType,
            Token ltToken,
            Node typeNode,
            Token gtToken) {
        Objects.requireNonNull(parameterizedType, "parameterizedType must not be null");
        Objects.requireNonNull(ltToken, "ltToken must not be null");
        Objects.requireNonNull(typeNode, "typeNode must not be null");
        Objects.requireNonNull(gtToken, "gtToken must not be null");

        STNode stParameterizedTypeDescriptorNode = STNodeFactory.createParameterizedTypeDescriptorNode(
                parameterizedType.internalNode(),
                ltToken.internalNode(),
                typeNode.internalNode(),
                gtToken.internalNode());
        return stParameterizedTypeDescriptorNode.createUnlinkedFacade();
    }

    public static NilLiteralNode createNilLiteralNode(
            Token openParenToken,
            Token closeParenToken) {
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stNilLiteralNode = STNodeFactory.createNilLiteralNode(
                openParenToken.internalNode(),
                closeParenToken.internalNode());
        return stNilLiteralNode.createUnlinkedFacade();
    }

    public static AnnotationDeclarationNode createAnnotationDeclarationNode(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token constKeyword,
            Token annotationKeyword,
            Node typeDescriptor,
            Token annotationTag,
            Token onKeyword,
            SeparatedNodeList<Node> attachPoints,
            Token semicolonToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
        Objects.requireNonNull(constKeyword, "constKeyword must not be null");
        Objects.requireNonNull(annotationKeyword, "annotationKeyword must not be null");
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(annotationTag, "annotationTag must not be null");
        Objects.requireNonNull(onKeyword, "onKeyword must not be null");
        Objects.requireNonNull(attachPoints, "attachPoints must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stAnnotationDeclarationNode = STNodeFactory.createAnnotationDeclarationNode(
                metadata.internalNode(),
                visibilityQualifier.internalNode(),
                constKeyword.internalNode(),
                annotationKeyword.internalNode(),
                typeDescriptor.internalNode(),
                annotationTag.internalNode(),
                onKeyword.internalNode(),
                attachPoints.underlyingListNode().internalNode(),
                semicolonToken.internalNode());
        return stAnnotationDeclarationNode.createUnlinkedFacade();
    }

    public static AnnotationAttachPointNode createAnnotationAttachPointNode(
            Token sourceKeyword,
            Token firstIdent,
            Token secondIdent) {
        Objects.requireNonNull(sourceKeyword, "sourceKeyword must not be null");
        Objects.requireNonNull(firstIdent, "firstIdent must not be null");
        Objects.requireNonNull(secondIdent, "secondIdent must not be null");

        STNode stAnnotationAttachPointNode = STNodeFactory.createAnnotationAttachPointNode(
                sourceKeyword.internalNode(),
                firstIdent.internalNode(),
                secondIdent.internalNode());
        return stAnnotationAttachPointNode.createUnlinkedFacade();
    }

    public static XMLNamespaceDeclarationNode createXMLNamespaceDeclarationNode(
            Token xmlnsKeyword,
            ExpressionNode namespaceuri,
            Token asKeyword,
            IdentifierToken namespacePrefix,
            Token semicolonToken) {
        Objects.requireNonNull(xmlnsKeyword, "xmlnsKeyword must not be null");
        Objects.requireNonNull(namespaceuri, "namespaceuri must not be null");
        Objects.requireNonNull(asKeyword, "asKeyword must not be null");
        Objects.requireNonNull(namespacePrefix, "namespacePrefix must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stXMLNamespaceDeclarationNode = STNodeFactory.createXMLNamespaceDeclarationNode(
                xmlnsKeyword.internalNode(),
                namespaceuri.internalNode(),
                asKeyword.internalNode(),
                namespacePrefix.internalNode(),
                semicolonToken.internalNode());
        return stXMLNamespaceDeclarationNode.createUnlinkedFacade();
    }

    public static ModuleXMLNamespaceDeclarationNode createModuleXMLNamespaceDeclarationNode(
            Token xmlnsKeyword,
            ExpressionNode namespaceuri,
            Token asKeyword,
            IdentifierToken namespacePrefix,
            Token semicolonToken) {
        Objects.requireNonNull(xmlnsKeyword, "xmlnsKeyword must not be null");
        Objects.requireNonNull(namespaceuri, "namespaceuri must not be null");
        Objects.requireNonNull(asKeyword, "asKeyword must not be null");
        Objects.requireNonNull(namespacePrefix, "namespacePrefix must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");

        STNode stModuleXMLNamespaceDeclarationNode = STNodeFactory.createModuleXMLNamespaceDeclarationNode(
                xmlnsKeyword.internalNode(),
                namespaceuri.internalNode(),
                asKeyword.internalNode(),
                namespacePrefix.internalNode(),
                semicolonToken.internalNode());
        return stModuleXMLNamespaceDeclarationNode.createUnlinkedFacade();
    }

    public static FunctionBodyBlockNode createFunctionBodyBlockNode(
            Token openBraceToken,
            NamedWorkerDeclarator namedWorkerDeclarator,
            NodeList<StatementNode> statements,
            Token closeBraceToken) {
        Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
        Objects.requireNonNull(statements, "statements must not be null");
        Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");

        STNode stFunctionBodyBlockNode = STNodeFactory.createFunctionBodyBlockNode(
                openBraceToken.internalNode(),
                getOptionalSTNode(namedWorkerDeclarator),
                statements.underlyingListNode().internalNode(),
                closeBraceToken.internalNode());
        return stFunctionBodyBlockNode.createUnlinkedFacade();
    }

    public static NamedWorkerDeclarationNode createNamedWorkerDeclarationNode(
            NodeList<AnnotationNode> annotations,
            Token workerKeyword,
            IdentifierToken workerName,
            Node returnTypeDesc,
            BlockStatementNode workerBody) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(workerKeyword, "workerKeyword must not be null");
        Objects.requireNonNull(workerName, "workerName must not be null");
        Objects.requireNonNull(workerBody, "workerBody must not be null");

        STNode stNamedWorkerDeclarationNode = STNodeFactory.createNamedWorkerDeclarationNode(
                annotations.underlyingListNode().internalNode(),
                workerKeyword.internalNode(),
                workerName.internalNode(),
                getOptionalSTNode(returnTypeDesc),
                workerBody.internalNode());
        return stNamedWorkerDeclarationNode.createUnlinkedFacade();
    }

    public static NamedWorkerDeclarator createNamedWorkerDeclarator(
            NodeList<StatementNode> workerInitStatements,
            NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations) {
        Objects.requireNonNull(workerInitStatements, "workerInitStatements must not be null");
        Objects.requireNonNull(namedWorkerDeclarations, "namedWorkerDeclarations must not be null");

        STNode stNamedWorkerDeclarator = STNodeFactory.createNamedWorkerDeclarator(
                workerInitStatements.underlyingListNode().internalNode(),
                namedWorkerDeclarations.underlyingListNode().internalNode());
        return stNamedWorkerDeclarator.createUnlinkedFacade();
    }

    public static DocumentationStringNode createDocumentationStringNode(
            NodeList<Token> documentationLines) {
        Objects.requireNonNull(documentationLines, "documentationLines must not be null");

        STNode stDocumentationStringNode = STNodeFactory.createDocumentationStringNode(
                documentationLines.underlyingListNode().internalNode());
        return stDocumentationStringNode.createUnlinkedFacade();
    }

    public static BasicLiteralNode createBasicLiteralNode(
            SyntaxKind kind,
            Token literalToken) {
        Objects.requireNonNull(literalToken, "literalToken must not be null");

        STNode stBasicLiteralNode = STNodeFactory.createBasicLiteralNode(
                kind,
                literalToken.internalNode());
        return stBasicLiteralNode.createUnlinkedFacade();
    }

    public static SimpleNameReferenceNode createSimpleNameReferenceNode(
            Token name) {
        Objects.requireNonNull(name, "name must not be null");

        STNode stSimpleNameReferenceNode = STNodeFactory.createSimpleNameReferenceNode(
                name.internalNode());
        return stSimpleNameReferenceNode.createUnlinkedFacade();
    }

    public static QualifiedNameReferenceNode createQualifiedNameReferenceNode(
            Token modulePrefix,
            Node colon,
            IdentifierToken identifier) {
        Objects.requireNonNull(modulePrefix, "modulePrefix must not be null");
        Objects.requireNonNull(colon, "colon must not be null");
        Objects.requireNonNull(identifier, "identifier must not be null");

        STNode stQualifiedNameReferenceNode = STNodeFactory.createQualifiedNameReferenceNode(
                modulePrefix.internalNode(),
                colon.internalNode(),
                identifier.internalNode());
        return stQualifiedNameReferenceNode.createUnlinkedFacade();
    }

    public static BuiltinSimpleNameReferenceNode createBuiltinSimpleNameReferenceNode(
            SyntaxKind kind,
            Token name) {
        Objects.requireNonNull(name, "name must not be null");

        STNode stBuiltinSimpleNameReferenceNode = STNodeFactory.createBuiltinSimpleNameReferenceNode(
                kind,
                name.internalNode());
        return stBuiltinSimpleNameReferenceNode.createUnlinkedFacade();
    }

    public static TrapExpressionNode createTrapExpressionNode(
            SyntaxKind kind,
            Token trapKeyword,
            ExpressionNode expression) {
        Objects.requireNonNull(trapKeyword, "trapKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stTrapExpressionNode = STNodeFactory.createTrapExpressionNode(
                kind,
                trapKeyword.internalNode(),
                expression.internalNode());
        return stTrapExpressionNode.createUnlinkedFacade();
    }

    public static ListConstructorExpressionNode createListConstructorExpressionNode(
            Token openBracket,
            SeparatedNodeList<Node> expressions,
            Token closeBracket) {
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(expressions, "expressions must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");

        STNode stListConstructorExpressionNode = STNodeFactory.createListConstructorExpressionNode(
                openBracket.internalNode(),
                expressions.underlyingListNode().internalNode(),
                closeBracket.internalNode());
        return stListConstructorExpressionNode.createUnlinkedFacade();
    }

    public static TypeCastExpressionNode createTypeCastExpressionNode(
            Token ltToken,
            TypeCastParamNode typeCastParam,
            Token gtToken,
            ExpressionNode expression) {
        Objects.requireNonNull(ltToken, "ltToken must not be null");
        Objects.requireNonNull(typeCastParam, "typeCastParam must not be null");
        Objects.requireNonNull(gtToken, "gtToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stTypeCastExpressionNode = STNodeFactory.createTypeCastExpressionNode(
                ltToken.internalNode(),
                typeCastParam.internalNode(),
                gtToken.internalNode(),
                expression.internalNode());
        return stTypeCastExpressionNode.createUnlinkedFacade();
    }

    public static TypeCastParamNode createTypeCastParamNode(
            NodeList<AnnotationNode> annotations,
            Node type) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(type, "type must not be null");

        STNode stTypeCastParamNode = STNodeFactory.createTypeCastParamNode(
                annotations.underlyingListNode().internalNode(),
                type.internalNode());
        return stTypeCastParamNode.createUnlinkedFacade();
    }

    public static UnionTypeDescriptorNode createUnionTypeDescriptorNode(
            TypeDescriptorNode leftTypeDesc,
            Token pipeToken,
            TypeDescriptorNode rightTypeDesc) {
        Objects.requireNonNull(leftTypeDesc, "leftTypeDesc must not be null");
        Objects.requireNonNull(pipeToken, "pipeToken must not be null");
        Objects.requireNonNull(rightTypeDesc, "rightTypeDesc must not be null");

        STNode stUnionTypeDescriptorNode = STNodeFactory.createUnionTypeDescriptorNode(
                leftTypeDesc.internalNode(),
                pipeToken.internalNode(),
                rightTypeDesc.internalNode());
        return stUnionTypeDescriptorNode.createUnlinkedFacade();
    }

    public static TableConstructorExpressionNode createTableConstructorExpressionNode(
            Token tableKeyword,
            KeySpecifierNode keySpecifier,
            Token openBracket,
            SeparatedNodeList<Node> mappingConstructors,
            Token closeBracket) {
        Objects.requireNonNull(tableKeyword, "tableKeyword must not be null");
        Objects.requireNonNull(keySpecifier, "keySpecifier must not be null");
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(mappingConstructors, "mappingConstructors must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");

        STNode stTableConstructorExpressionNode = STNodeFactory.createTableConstructorExpressionNode(
                tableKeyword.internalNode(),
                keySpecifier.internalNode(),
                openBracket.internalNode(),
                mappingConstructors.underlyingListNode().internalNode(),
                closeBracket.internalNode());
        return stTableConstructorExpressionNode.createUnlinkedFacade();
    }

    public static KeySpecifierNode createKeySpecifierNode(
            Token keyKeyword,
            Token openParenToken,
            SeparatedNodeList<IdentifierToken> fieldNames,
            Token closeParenToken) {
        Objects.requireNonNull(keyKeyword, "keyKeyword must not be null");
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(fieldNames, "fieldNames must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stKeySpecifierNode = STNodeFactory.createKeySpecifierNode(
                keyKeyword.internalNode(),
                openParenToken.internalNode(),
                fieldNames.underlyingListNode().internalNode(),
                closeParenToken.internalNode());
        return stKeySpecifierNode.createUnlinkedFacade();
    }

    public static ErrorTypeDescriptorNode createErrorTypeDescriptorNode(
            Token errorKeywordToken,
            ErrorTypeParamsNode errorTypeParamsNode) {
        Objects.requireNonNull(errorKeywordToken, "errorKeywordToken must not be null");

        STNode stErrorTypeDescriptorNode = STNodeFactory.createErrorTypeDescriptorNode(
                errorKeywordToken.internalNode(),
                getOptionalSTNode(errorTypeParamsNode));
        return stErrorTypeDescriptorNode.createUnlinkedFacade();
    }

    public static ErrorTypeParamsNode createErrorTypeParamsNode(
            Token ltToken,
            Node parameter,
            Token gtToken) {
        Objects.requireNonNull(ltToken, "ltToken must not be null");
        Objects.requireNonNull(parameter, "parameter must not be null");
        Objects.requireNonNull(gtToken, "gtToken must not be null");

        STNode stErrorTypeParamsNode = STNodeFactory.createErrorTypeParamsNode(
                ltToken.internalNode(),
                parameter.internalNode(),
                gtToken.internalNode());
        return stErrorTypeParamsNode.createUnlinkedFacade();
    }

    public static StreamTypeDescriptorNode createStreamTypeDescriptorNode(
            Token streamKeywordToken,
            Node streamTypeParamsNode) {
        Objects.requireNonNull(streamKeywordToken, "streamKeywordToken must not be null");
        Objects.requireNonNull(streamTypeParamsNode, "streamTypeParamsNode must not be null");

        STNode stStreamTypeDescriptorNode = STNodeFactory.createStreamTypeDescriptorNode(
                streamKeywordToken.internalNode(),
                streamTypeParamsNode.internalNode());
        return stStreamTypeDescriptorNode.createUnlinkedFacade();
    }

    public static StreamTypeParamsNode createStreamTypeParamsNode(
            Token ltToken,
            Node leftTypeDescNode,
            Token commaToken,
            Node rightTypeDescNode,
            Token gtToken) {
        Objects.requireNonNull(ltToken, "ltToken must not be null");
        Objects.requireNonNull(leftTypeDescNode, "leftTypeDescNode must not be null");
        Objects.requireNonNull(commaToken, "commaToken must not be null");
        Objects.requireNonNull(rightTypeDescNode, "rightTypeDescNode must not be null");
        Objects.requireNonNull(gtToken, "gtToken must not be null");

        STNode stStreamTypeParamsNode = STNodeFactory.createStreamTypeParamsNode(
                ltToken.internalNode(),
                leftTypeDescNode.internalNode(),
                commaToken.internalNode(),
                rightTypeDescNode.internalNode(),
                gtToken.internalNode());
        return stStreamTypeParamsNode.createUnlinkedFacade();
    }

    public static LetExpressionNode createLetExpressionNode(
            Token letKeyword,
            SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations,
            Token inKeyword,
            ExpressionNode expression) {
        Objects.requireNonNull(letKeyword, "letKeyword must not be null");
        Objects.requireNonNull(letVarDeclarations, "letVarDeclarations must not be null");
        Objects.requireNonNull(inKeyword, "inKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stLetExpressionNode = STNodeFactory.createLetExpressionNode(
                letKeyword.internalNode(),
                letVarDeclarations.underlyingListNode().internalNode(),
                inKeyword.internalNode(),
                expression.internalNode());
        return stLetExpressionNode.createUnlinkedFacade();
    }

    public static LetVariableDeclarationNode createLetVariableDeclarationNode(
            NodeList<AnnotationNode> annotations,
            TypedBindingPatternNode typedBindingPattern,
            Token equalsToken,
            ExpressionNode expression) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stLetVariableDeclarationNode = STNodeFactory.createLetVariableDeclarationNode(
                annotations.underlyingListNode().internalNode(),
                typedBindingPattern.internalNode(),
                equalsToken.internalNode(),
                expression.internalNode());
        return stLetVariableDeclarationNode.createUnlinkedFacade();
    }

    public static TemplateExpressionNode createTemplateExpressionNode(
            SyntaxKind kind,
            Token type,
            Token startBacktick,
            NodeList<TemplateMemberNode> content,
            Token endBacktick) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(startBacktick, "startBacktick must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(endBacktick, "endBacktick must not be null");

        STNode stTemplateExpressionNode = STNodeFactory.createTemplateExpressionNode(
                kind,
                type.internalNode(),
                startBacktick.internalNode(),
                content.underlyingListNode().internalNode(),
                endBacktick.internalNode());
        return stTemplateExpressionNode.createUnlinkedFacade();
    }

    public static XMLElementNode createXMLElementNode(
            XMLStartTagNode startTag,
            NodeList<XMLItemNode> content,
            XMLEndTagNode endTag) {
        Objects.requireNonNull(startTag, "startTag must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(endTag, "endTag must not be null");

        STNode stXMLElementNode = STNodeFactory.createXMLElementNode(
                startTag.internalNode(),
                content.underlyingListNode().internalNode(),
                endTag.internalNode());
        return stXMLElementNode.createUnlinkedFacade();
    }

    public static XMLStartTagNode createXMLStartTagNode(
            Token ltToken,
            XMLNameNode name,
            NodeList<XMLAttributeNode> attributes,
            Token getToken) {
        Objects.requireNonNull(ltToken, "ltToken must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(attributes, "attributes must not be null");
        Objects.requireNonNull(getToken, "getToken must not be null");

        STNode stXMLStartTagNode = STNodeFactory.createXMLStartTagNode(
                ltToken.internalNode(),
                name.internalNode(),
                attributes.underlyingListNode().internalNode(),
                getToken.internalNode());
        return stXMLStartTagNode.createUnlinkedFacade();
    }

    public static XMLEndTagNode createXMLEndTagNode(
            Token ltToken,
            Token slashToken,
            XMLNameNode name,
            Token getToken) {
        Objects.requireNonNull(ltToken, "ltToken must not be null");
        Objects.requireNonNull(slashToken, "slashToken must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(getToken, "getToken must not be null");

        STNode stXMLEndTagNode = STNodeFactory.createXMLEndTagNode(
                ltToken.internalNode(),
                slashToken.internalNode(),
                name.internalNode(),
                getToken.internalNode());
        return stXMLEndTagNode.createUnlinkedFacade();
    }

    public static XMLSimpleNameNode createXMLSimpleNameNode(
            Token name) {
        Objects.requireNonNull(name, "name must not be null");

        STNode stXMLSimpleNameNode = STNodeFactory.createXMLSimpleNameNode(
                name.internalNode());
        return stXMLSimpleNameNode.createUnlinkedFacade();
    }

    public static XMLQualifiedNameNode createXMLQualifiedNameNode(
            XMLSimpleNameNode prefix,
            Token colon,
            XMLSimpleNameNode name) {
        Objects.requireNonNull(prefix, "prefix must not be null");
        Objects.requireNonNull(colon, "colon must not be null");
        Objects.requireNonNull(name, "name must not be null");

        STNode stXMLQualifiedNameNode = STNodeFactory.createXMLQualifiedNameNode(
                prefix.internalNode(),
                colon.internalNode(),
                name.internalNode());
        return stXMLQualifiedNameNode.createUnlinkedFacade();
    }

    public static XMLEmptyElementNode createXMLEmptyElementNode(
            Token ltToken,
            XMLNameNode name,
            NodeList<XMLAttributeNode> attributes,
            Token slashToken,
            Token getToken) {
        Objects.requireNonNull(ltToken, "ltToken must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(attributes, "attributes must not be null");
        Objects.requireNonNull(slashToken, "slashToken must not be null");
        Objects.requireNonNull(getToken, "getToken must not be null");

        STNode stXMLEmptyElementNode = STNodeFactory.createXMLEmptyElementNode(
                ltToken.internalNode(),
                name.internalNode(),
                attributes.underlyingListNode().internalNode(),
                slashToken.internalNode(),
                getToken.internalNode());
        return stXMLEmptyElementNode.createUnlinkedFacade();
    }

    public static InterpolationNode createInterpolationNode(
            Token interpolationStartToken,
            ExpressionNode expression,
            Token interpolationEndToken) {
        Objects.requireNonNull(interpolationStartToken, "interpolationStartToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(interpolationEndToken, "interpolationEndToken must not be null");

        STNode stInterpolationNode = STNodeFactory.createInterpolationNode(
                interpolationStartToken.internalNode(),
                expression.internalNode(),
                interpolationEndToken.internalNode());
        return stInterpolationNode.createUnlinkedFacade();
    }

    public static XMLTextNode createXMLTextNode(
            Token content) {
        Objects.requireNonNull(content, "content must not be null");

        STNode stXMLTextNode = STNodeFactory.createXMLTextNode(
                content.internalNode());
        return stXMLTextNode.createUnlinkedFacade();
    }

    public static XMLAttributeNode createXMLAttributeNode(
            XMLNameNode attributeName,
            Token equalToken,
            XMLAttributeValue value) {
        Objects.requireNonNull(attributeName, "attributeName must not be null");
        Objects.requireNonNull(equalToken, "equalToken must not be null");
        Objects.requireNonNull(value, "value must not be null");

        STNode stXMLAttributeNode = STNodeFactory.createXMLAttributeNode(
                attributeName.internalNode(),
                equalToken.internalNode(),
                value.internalNode());
        return stXMLAttributeNode.createUnlinkedFacade();
    }

    public static XMLAttributeValue createXMLAttributeValue(
            Token startQuote,
            NodeList<Node> value,
            Token endQuote) {
        Objects.requireNonNull(startQuote, "startQuote must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(endQuote, "endQuote must not be null");

        STNode stXMLAttributeValue = STNodeFactory.createXMLAttributeValue(
                startQuote.internalNode(),
                value.underlyingListNode().internalNode(),
                endQuote.internalNode());
        return stXMLAttributeValue.createUnlinkedFacade();
    }

    public static XMLComment createXMLComment(
            Token commentStart,
            NodeList<Node> content,
            Token commentEnd) {
        Objects.requireNonNull(commentStart, "commentStart must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(commentEnd, "commentEnd must not be null");

        STNode stXMLComment = STNodeFactory.createXMLComment(
                commentStart.internalNode(),
                content.underlyingListNode().internalNode(),
                commentEnd.internalNode());
        return stXMLComment.createUnlinkedFacade();
    }

    public static XMLProcessingInstruction createXMLProcessingInstruction(
            Token piStart,
            XMLNameNode target,
            NodeList<Node> data,
            Token piEnd) {
        Objects.requireNonNull(piStart, "piStart must not be null");
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(piEnd, "piEnd must not be null");

        STNode stXMLProcessingInstruction = STNodeFactory.createXMLProcessingInstruction(
                piStart.internalNode(),
                target.internalNode(),
                data.underlyingListNode().internalNode(),
                piEnd.internalNode());
        return stXMLProcessingInstruction.createUnlinkedFacade();
    }

    public static TableTypeDescriptorNode createTableTypeDescriptorNode(
            Token tableKeywordToken,
            Node rowTypeParameterNode,
            Node keyConstraintNode) {
        Objects.requireNonNull(tableKeywordToken, "tableKeywordToken must not be null");
        Objects.requireNonNull(rowTypeParameterNode, "rowTypeParameterNode must not be null");
        Objects.requireNonNull(keyConstraintNode, "keyConstraintNode must not be null");

        STNode stTableTypeDescriptorNode = STNodeFactory.createTableTypeDescriptorNode(
                tableKeywordToken.internalNode(),
                rowTypeParameterNode.internalNode(),
                keyConstraintNode.internalNode());
        return stTableTypeDescriptorNode.createUnlinkedFacade();
    }

    public static TypeParameterNode createTypeParameterNode(
            Token ltToken,
            Node typeNode,
            Token gtToken) {
        Objects.requireNonNull(ltToken, "ltToken must not be null");
        Objects.requireNonNull(typeNode, "typeNode must not be null");
        Objects.requireNonNull(gtToken, "gtToken must not be null");

        STNode stTypeParameterNode = STNodeFactory.createTypeParameterNode(
                ltToken.internalNode(),
                typeNode.internalNode(),
                gtToken.internalNode());
        return stTypeParameterNode.createUnlinkedFacade();
    }

    public static KeyTypeConstraintNode createKeyTypeConstraintNode(
            Token keyKeywordToken,
            Node typeParameterNode) {
        Objects.requireNonNull(keyKeywordToken, "keyKeywordToken must not be null");
        Objects.requireNonNull(typeParameterNode, "typeParameterNode must not be null");

        STNode stKeyTypeConstraintNode = STNodeFactory.createKeyTypeConstraintNode(
                keyKeywordToken.internalNode(),
                typeParameterNode.internalNode());
        return stKeyTypeConstraintNode.createUnlinkedFacade();
    }

    public static FunctionTypeDescriptorNode createFunctionTypeDescriptorNode(
            Token functionKeyword,
            FunctionSignatureNode functionSignature) {
        Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
        Objects.requireNonNull(functionSignature, "functionSignature must not be null");

        STNode stFunctionTypeDescriptorNode = STNodeFactory.createFunctionTypeDescriptorNode(
                functionKeyword.internalNode(),
                functionSignature.internalNode());
        return stFunctionTypeDescriptorNode.createUnlinkedFacade();
    }

    public static FunctionSignatureNode createFunctionSignatureNode(
            Token openParenToken,
            NodeList<ParameterNode> parameters,
            Token closeParenToken,
            ReturnTypeDescriptorNode returnTypeDesc) {
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(parameters, "parameters must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stFunctionSignatureNode = STNodeFactory.createFunctionSignatureNode(
                openParenToken.internalNode(),
                parameters.underlyingListNode().internalNode(),
                closeParenToken.internalNode(),
                getOptionalSTNode(returnTypeDesc));
        return stFunctionSignatureNode.createUnlinkedFacade();
    }

    public static ExplicitAnonymousFunctionExpressionNode createExplicitAnonymousFunctionExpressionNode(
            NodeList<AnnotationNode> annotations,
            Token functionKeyword,
            FunctionSignatureNode functionSignature,
            FunctionBodyNode functionBody) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
        Objects.requireNonNull(functionSignature, "functionSignature must not be null");
        Objects.requireNonNull(functionBody, "functionBody must not be null");

        STNode stExplicitAnonymousFunctionExpressionNode = STNodeFactory.createExplicitAnonymousFunctionExpressionNode(
                annotations.underlyingListNode().internalNode(),
                functionKeyword.internalNode(),
                functionSignature.internalNode(),
                functionBody.internalNode());
        return stExplicitAnonymousFunctionExpressionNode.createUnlinkedFacade();
    }

    public static ExpressionFunctionBodyNode createExpressionFunctionBodyNode(
            Token rightDoubleArrow,
            ExpressionNode expression,
            Token semicolon) {
        Objects.requireNonNull(rightDoubleArrow, "rightDoubleArrow must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stExpressionFunctionBodyNode = STNodeFactory.createExpressionFunctionBodyNode(
                rightDoubleArrow.internalNode(),
                expression.internalNode(),
                getOptionalSTNode(semicolon));
        return stExpressionFunctionBodyNode.createUnlinkedFacade();
    }

    public static TupleTypeDescriptorNode createTupleTypeDescriptorNode(
            Token openBracketToken,
            SeparatedNodeList<Node> memberTypeDesc,
            Token closeBracketToken) {
        Objects.requireNonNull(openBracketToken, "openBracketToken must not be null");
        Objects.requireNonNull(memberTypeDesc, "memberTypeDesc must not be null");
        Objects.requireNonNull(closeBracketToken, "closeBracketToken must not be null");

        STNode stTupleTypeDescriptorNode = STNodeFactory.createTupleTypeDescriptorNode(
                openBracketToken.internalNode(),
                memberTypeDesc.underlyingListNode().internalNode(),
                closeBracketToken.internalNode());
        return stTupleTypeDescriptorNode.createUnlinkedFacade();
    }

    public static ParenthesisedTypeDescriptorNode createParenthesisedTypeDescriptorNode(
            Token openParenToken,
            TypeDescriptorNode typedesc,
            Token closeParenToken) {
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(typedesc, "typedesc must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stParenthesisedTypeDescriptorNode = STNodeFactory.createParenthesisedTypeDescriptorNode(
                openParenToken.internalNode(),
                typedesc.internalNode(),
                closeParenToken.internalNode());
        return stParenthesisedTypeDescriptorNode.createUnlinkedFacade();
    }

    public static ExplicitNewExpressionNode createExplicitNewExpressionNode(
            Token newKeyword,
            TypeDescriptorNode typeDescriptor,
            Node parenthesizedArgList) {
        Objects.requireNonNull(newKeyword, "newKeyword must not be null");
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(parenthesizedArgList, "parenthesizedArgList must not be null");

        STNode stExplicitNewExpressionNode = STNodeFactory.createExplicitNewExpressionNode(
                newKeyword.internalNode(),
                typeDescriptor.internalNode(),
                parenthesizedArgList.internalNode());
        return stExplicitNewExpressionNode.createUnlinkedFacade();
    }

    public static ImplicitNewExpressionNode createImplicitNewExpressionNode(
            Token newKeyword,
            ParenthesizedArgList parenthesizedArgList) {
        Objects.requireNonNull(newKeyword, "newKeyword must not be null");

        STNode stImplicitNewExpressionNode = STNodeFactory.createImplicitNewExpressionNode(
                newKeyword.internalNode(),
                getOptionalSTNode(parenthesizedArgList));
        return stImplicitNewExpressionNode.createUnlinkedFacade();
    }

    public static ParenthesizedArgList createParenthesizedArgList(
            Token openParenToken,
            NodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stParenthesizedArgList = STNodeFactory.createParenthesizedArgList(
                openParenToken.internalNode(),
                arguments.underlyingListNode().internalNode(),
                closeParenToken.internalNode());
        return stParenthesizedArgList.createUnlinkedFacade();
    }

    public static QueryConstructTypeNode createQueryConstructTypeNode(
            Token tableKeyword,
            KeySpecifierNode keySpecifier) {
        Objects.requireNonNull(tableKeyword, "tableKeyword must not be null");
        Objects.requireNonNull(keySpecifier, "keySpecifier must not be null");

        STNode stQueryConstructTypeNode = STNodeFactory.createQueryConstructTypeNode(
                tableKeyword.internalNode(),
                keySpecifier.internalNode());
        return stQueryConstructTypeNode.createUnlinkedFacade();
    }

    public static FromClauseNode createFromClauseNode(
            Token fromKeyword,
            TypedBindingPatternNode typedBindingPattern,
            Token inKeyword,
            ExpressionNode expression) {
        Objects.requireNonNull(fromKeyword, "fromKeyword must not be null");
        Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
        Objects.requireNonNull(inKeyword, "inKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stFromClauseNode = STNodeFactory.createFromClauseNode(
                fromKeyword.internalNode(),
                typedBindingPattern.internalNode(),
                inKeyword.internalNode(),
                expression.internalNode());
        return stFromClauseNode.createUnlinkedFacade();
    }

    public static WhereClauseNode createWhereClauseNode(
            Token whereKeyword,
            ExpressionNode expression) {
        Objects.requireNonNull(whereKeyword, "whereKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stWhereClauseNode = STNodeFactory.createWhereClauseNode(
                whereKeyword.internalNode(),
                expression.internalNode());
        return stWhereClauseNode.createUnlinkedFacade();
    }

    public static LetClauseNode createLetClauseNode(
            Token letKeyword,
            SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations) {
        Objects.requireNonNull(letKeyword, "letKeyword must not be null");
        Objects.requireNonNull(letVarDeclarations, "letVarDeclarations must not be null");

        STNode stLetClauseNode = STNodeFactory.createLetClauseNode(
                letKeyword.internalNode(),
                letVarDeclarations.underlyingListNode().internalNode());
        return stLetClauseNode.createUnlinkedFacade();
    }

    public static QueryPipelineNode createQueryPipelineNode(
            FromClauseNode fromClause,
            NodeList<Node> intermediateClauses) {
        Objects.requireNonNull(fromClause, "fromClause must not be null");
        Objects.requireNonNull(intermediateClauses, "intermediateClauses must not be null");

        STNode stQueryPipelineNode = STNodeFactory.createQueryPipelineNode(
                fromClause.internalNode(),
                intermediateClauses.underlyingListNode().internalNode());
        return stQueryPipelineNode.createUnlinkedFacade();
    }

    public static SelectClauseNode createSelectClauseNode(
            Token selectKeyword,
            ExpressionNode expression) {
        Objects.requireNonNull(selectKeyword, "selectKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stSelectClauseNode = STNodeFactory.createSelectClauseNode(
                selectKeyword.internalNode(),
                expression.internalNode());
        return stSelectClauseNode.createUnlinkedFacade();
    }

    public static QueryExpressionNode createQueryExpressionNode(
            QueryConstructTypeNode queryConstructType,
            QueryPipelineNode queryPipeline,
            SelectClauseNode selectClause) {
        Objects.requireNonNull(queryConstructType, "queryConstructType must not be null");
        Objects.requireNonNull(queryPipeline, "queryPipeline must not be null");
        Objects.requireNonNull(selectClause, "selectClause must not be null");

        STNode stQueryExpressionNode = STNodeFactory.createQueryExpressionNode(
                queryConstructType.internalNode(),
                queryPipeline.internalNode(),
                selectClause.internalNode());
        return stQueryExpressionNode.createUnlinkedFacade();
    }

    public static IntersectionTypeDescriptorNode createIntersectionTypeDescriptorNode(
            Node leftTypeDesc,
            Token bitwiseAndToken,
            Node rightTypeDesc) {
        Objects.requireNonNull(leftTypeDesc, "leftTypeDesc must not be null");
        Objects.requireNonNull(bitwiseAndToken, "bitwiseAndToken must not be null");
        Objects.requireNonNull(rightTypeDesc, "rightTypeDesc must not be null");

        STNode stIntersectionTypeDescriptorNode = STNodeFactory.createIntersectionTypeDescriptorNode(
                leftTypeDesc.internalNode(),
                bitwiseAndToken.internalNode(),
                rightTypeDesc.internalNode());
        return stIntersectionTypeDescriptorNode.createUnlinkedFacade();
    }

    public static ImplicitAnonymousFunctionParameters createImplicitAnonymousFunctionParameters(
            Token openParenToken,
            SeparatedNodeList<SimpleNameReferenceNode> parameters,
            Token closeParenToken) {
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(parameters, "parameters must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");

        STNode stImplicitAnonymousFunctionParameters = STNodeFactory.createImplicitAnonymousFunctionParameters(
                openParenToken.internalNode(),
                parameters.underlyingListNode().internalNode(),
                closeParenToken.internalNode());
        return stImplicitAnonymousFunctionParameters.createUnlinkedFacade();
    }

    public static ImplicitAnonymousFunctionExpressionNode createImplicitAnonymousFunctionExpressionNode(
            Node params,
            Token rightDoubleArrow,
            ExpressionNode expression) {
        Objects.requireNonNull(params, "params must not be null");
        Objects.requireNonNull(rightDoubleArrow, "rightDoubleArrow must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stImplicitAnonymousFunctionExpressionNode = STNodeFactory.createImplicitAnonymousFunctionExpressionNode(
                params.internalNode(),
                rightDoubleArrow.internalNode(),
                expression.internalNode());
        return stImplicitAnonymousFunctionExpressionNode.createUnlinkedFacade();
    }

    public static StartActionNode createStartActionNode(
            NodeList<AnnotationNode> annotations,
            Token startKeyword,
            ExpressionNode expression) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(startKeyword, "startKeyword must not be null");
        Objects.requireNonNull(expression, "expression must not be null");

        STNode stStartActionNode = STNodeFactory.createStartActionNode(
                annotations.underlyingListNode().internalNode(),
                startKeyword.internalNode(),
                expression.internalNode());
        return stStartActionNode.createUnlinkedFacade();
    }

    public static FlushActionNode createFlushActionNode(
            Token flushKeyword,
            NameReferenceNode peerWorker) {
        Objects.requireNonNull(flushKeyword, "flushKeyword must not be null");
        Objects.requireNonNull(peerWorker, "peerWorker must not be null");

        STNode stFlushActionNode = STNodeFactory.createFlushActionNode(
                flushKeyword.internalNode(),
                peerWorker.internalNode());
        return stFlushActionNode.createUnlinkedFacade();
    }

    public static SingletonTypeDescriptorNode createSingletonTypeDescriptorNode(
            ExpressionNode simpleContExprNode) {
        Objects.requireNonNull(simpleContExprNode, "simpleContExprNode must not be null");

        STNode stSingletonTypeDescriptorNode = STNodeFactory.createSingletonTypeDescriptorNode(
                simpleContExprNode.internalNode());
        return stSingletonTypeDescriptorNode.createUnlinkedFacade();
    }

    public static MethodDeclarationNode createMethodDeclarationNode(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token functionKeyword,
            IdentifierToken methodName,
            FunctionSignatureNode methodSignature,
            Token semicolon) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
        Objects.requireNonNull(methodName, "methodName must not be null");
        Objects.requireNonNull(methodSignature, "methodSignature must not be null");
        Objects.requireNonNull(semicolon, "semicolon must not be null");

        STNode stMethodDeclarationNode = STNodeFactory.createMethodDeclarationNode(
                metadata.internalNode(),
                getOptionalSTNode(visibilityQualifier),
                functionKeyword.internalNode(),
                methodName.internalNode(),
                methodSignature.internalNode(),
                semicolon.internalNode());
        return stMethodDeclarationNode.createUnlinkedFacade();
    }

    public static TypedBindingPatternNode createTypedBindingPatternNode(
            TypeDescriptorNode typeDescriptor,
            BindingPatternNode bindingPattern) {
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(bindingPattern, "bindingPattern must not be null");

        STNode stTypedBindingPatternNode = STNodeFactory.createTypedBindingPatternNode(
                typeDescriptor.internalNode(),
                bindingPattern.internalNode());
        return stTypedBindingPatternNode.createUnlinkedFacade();
    }

    public static CaptureBindingPatternNode createCaptureBindingPatternNode(
            Token variableName) {
        Objects.requireNonNull(variableName, "variableName must not be null");

        STNode stCaptureBindingPatternNode = STNodeFactory.createCaptureBindingPatternNode(
                variableName.internalNode());
        return stCaptureBindingPatternNode.createUnlinkedFacade();
    }

    public static WildcardBindingPatternNode createWildcardBindingPatternNode(
            Token underscoreToken) {
        Objects.requireNonNull(underscoreToken, "underscoreToken must not be null");

        STNode stWildcardBindingPatternNode = STNodeFactory.createWildcardBindingPatternNode(
                underscoreToken.internalNode());
        return stWildcardBindingPatternNode.createUnlinkedFacade();
    }

    public static ListBindingPatternNode createListBindingPatternNode(
            Token openBracket,
            SeparatedNodeList<BindingPatternNode> bindingPatterns,
            RestBindingPatternNode restBindingPattern,
            Token closeBracket) {
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(bindingPatterns, "bindingPatterns must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");

        STNode stListBindingPatternNode = STNodeFactory.createListBindingPatternNode(
                openBracket.internalNode(),
                bindingPatterns.underlyingListNode().internalNode(),
                getOptionalSTNode(restBindingPattern),
                closeBracket.internalNode());
        return stListBindingPatternNode.createUnlinkedFacade();
    }

    public static MappingBindingPatternNode createMappingBindingPatternNode(
            Token openBrace,
            SeparatedNodeList<FieldBindingPatternNode> fieldBindingPatterns,
            RestBindingPatternNode restBindingPattern,
            Token closeBrace) {
        Objects.requireNonNull(openBrace, "openBrace must not be null");
        Objects.requireNonNull(fieldBindingPatterns, "fieldBindingPatterns must not be null");
        Objects.requireNonNull(closeBrace, "closeBrace must not be null");

        STNode stMappingBindingPatternNode = STNodeFactory.createMappingBindingPatternNode(
                openBrace.internalNode(),
                fieldBindingPatterns.underlyingListNode().internalNode(),
                getOptionalSTNode(restBindingPattern),
                closeBrace.internalNode());
        return stMappingBindingPatternNode.createUnlinkedFacade();
    }

    public static FieldBindingPatternFullNode createFieldBindingPatternFullNode(
            SimpleNameReferenceNode variableName,
            Token colon,
            BindingPatternNode bindingPattern) {
        Objects.requireNonNull(variableName, "variableName must not be null");
        Objects.requireNonNull(colon, "colon must not be null");
        Objects.requireNonNull(bindingPattern, "bindingPattern must not be null");

        STNode stFieldBindingPatternFullNode = STNodeFactory.createFieldBindingPatternFullNode(
                variableName.internalNode(),
                colon.internalNode(),
                bindingPattern.internalNode());
        return stFieldBindingPatternFullNode.createUnlinkedFacade();
    }

    public static FieldBindingPatternVarnameNode createFieldBindingPatternVarnameNode(
            SimpleNameReferenceNode variableName) {
        Objects.requireNonNull(variableName, "variableName must not be null");

        STNode stFieldBindingPatternVarnameNode = STNodeFactory.createFieldBindingPatternVarnameNode(
                variableName.internalNode());
        return stFieldBindingPatternVarnameNode.createUnlinkedFacade();
    }

    public static RestBindingPatternNode createRestBindingPatternNode(
            Token ellipsisToken,
            SimpleNameReferenceNode variableName) {
        Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");
        Objects.requireNonNull(variableName, "variableName must not be null");

        STNode stRestBindingPatternNode = STNodeFactory.createRestBindingPatternNode(
                ellipsisToken.internalNode(),
                variableName.internalNode());
        return stRestBindingPatternNode.createUnlinkedFacade();
    }

    public static AsyncSendActionNode createAsyncSendActionNode(
            ExpressionNode expression,
            Token rightArrowToken,
            SimpleNameReferenceNode peerWorker) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(rightArrowToken, "rightArrowToken must not be null");
        Objects.requireNonNull(peerWorker, "peerWorker must not be null");

        STNode stAsyncSendActionNode = STNodeFactory.createAsyncSendActionNode(
                expression.internalNode(),
                rightArrowToken.internalNode(),
                peerWorker.internalNode());
        return stAsyncSendActionNode.createUnlinkedFacade();
    }

    public static SyncSendActionNode createSyncSendActionNode(
            ExpressionNode expression,
            Token syncSendToken,
            SimpleNameReferenceNode peerWorker) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(syncSendToken, "syncSendToken must not be null");
        Objects.requireNonNull(peerWorker, "peerWorker must not be null");

        STNode stSyncSendActionNode = STNodeFactory.createSyncSendActionNode(
                expression.internalNode(),
                syncSendToken.internalNode(),
                peerWorker.internalNode());
        return stSyncSendActionNode.createUnlinkedFacade();
    }

    public static ReceiveActionNode createReceiveActionNode(
            Token leftArrow,
            SimpleNameReferenceNode receiveWorkers) {
        Objects.requireNonNull(leftArrow, "leftArrow must not be null");
        Objects.requireNonNull(receiveWorkers, "receiveWorkers must not be null");

        STNode stReceiveActionNode = STNodeFactory.createReceiveActionNode(
                leftArrow.internalNode(),
                receiveWorkers.internalNode());
        return stReceiveActionNode.createUnlinkedFacade();
    }

    public static ReceiveFieldsNode createReceiveFieldsNode(
            Token openBrace,
            SeparatedNodeList<NameReferenceNode> receiveFields,
            Token closeBrace) {
        Objects.requireNonNull(openBrace, "openBrace must not be null");
        Objects.requireNonNull(receiveFields, "receiveFields must not be null");
        Objects.requireNonNull(closeBrace, "closeBrace must not be null");

        STNode stReceiveFieldsNode = STNodeFactory.createReceiveFieldsNode(
                openBrace.internalNode(),
                receiveFields.underlyingListNode().internalNode(),
                closeBrace.internalNode());
        return stReceiveFieldsNode.createUnlinkedFacade();
    }

    public static RestDescriptorNode createRestDescriptorNode(
            TypeDescriptorNode typeDescriptor,
            Token ellipsisToken) {
        Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
        Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");

        STNode stRestDescriptorNode = STNodeFactory.createRestDescriptorNode(
                typeDescriptor.internalNode(),
                ellipsisToken.internalNode());
        return stRestDescriptorNode.createUnlinkedFacade();
    }

    public static DoubleGTTokenNode createDoubleGTTokenNode(
            Token openGTToken,
            Token endGTToken) {
        Objects.requireNonNull(openGTToken, "openGTToken must not be null");
        Objects.requireNonNull(endGTToken, "endGTToken must not be null");

        STNode stDoubleGTTokenNode = STNodeFactory.createDoubleGTTokenNode(
                openGTToken.internalNode(),
                endGTToken.internalNode());
        return stDoubleGTTokenNode.createUnlinkedFacade();
    }

    public static TrippleGTTokenNode createTrippleGTTokenNode(
            Token openGTToken,
            Token middleGTToken,
            Token endGTToken) {
        Objects.requireNonNull(openGTToken, "openGTToken must not be null");
        Objects.requireNonNull(middleGTToken, "middleGTToken must not be null");
        Objects.requireNonNull(endGTToken, "endGTToken must not be null");

        STNode stTrippleGTTokenNode = STNodeFactory.createTrippleGTTokenNode(
                openGTToken.internalNode(),
                middleGTToken.internalNode(),
                endGTToken.internalNode());
        return stTrippleGTTokenNode.createUnlinkedFacade();
    }

    public static WaitActionNode createWaitActionNode(
            Token waitKeyword,
            Node waitFutureExpr) {
        Objects.requireNonNull(waitKeyword, "waitKeyword must not be null");
        Objects.requireNonNull(waitFutureExpr, "waitFutureExpr must not be null");

        STNode stWaitActionNode = STNodeFactory.createWaitActionNode(
                waitKeyword.internalNode(),
                waitFutureExpr.internalNode());
        return stWaitActionNode.createUnlinkedFacade();
    }

    public static WaitFieldsListNode createWaitFieldsListNode(
            Token openBrace,
            SeparatedNodeList<Node> waitFields,
            Token closeBrace) {
        Objects.requireNonNull(openBrace, "openBrace must not be null");
        Objects.requireNonNull(waitFields, "waitFields must not be null");
        Objects.requireNonNull(closeBrace, "closeBrace must not be null");

        STNode stWaitFieldsListNode = STNodeFactory.createWaitFieldsListNode(
                openBrace.internalNode(),
                waitFields.underlyingListNode().internalNode(),
                closeBrace.internalNode());
        return stWaitFieldsListNode.createUnlinkedFacade();
    }

    public static WaitFieldNode createWaitFieldNode(
            NameReferenceNode fieldName,
            Token colon,
            ExpressionNode waitFutureExpr) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(colon, "colon must not be null");
        Objects.requireNonNull(waitFutureExpr, "waitFutureExpr must not be null");

        STNode stWaitFieldNode = STNodeFactory.createWaitFieldNode(
                fieldName.internalNode(),
                colon.internalNode(),
                waitFutureExpr.internalNode());
        return stWaitFieldNode.createUnlinkedFacade();
    }

    public static AnnotAccessExpressionNode createAnnotAccessExpressionNode(
            ExpressionNode expression,
            Token annotChainingToken,
            Node annotTagReference) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(annotChainingToken, "annotChainingToken must not be null");
        Objects.requireNonNull(annotTagReference, "annotTagReference must not be null");

        STNode stAnnotAccessExpressionNode = STNodeFactory.createAnnotAccessExpressionNode(
                expression.internalNode(),
                annotChainingToken.internalNode(),
                annotTagReference.internalNode());
        return stAnnotAccessExpressionNode.createUnlinkedFacade();
    }

    public static QueryActionNode createQueryActionNode(
            QueryPipelineNode queryPipeline,
            Token doKeyword,
            BlockStatementNode blockStatement) {
        Objects.requireNonNull(queryPipeline, "queryPipeline must not be null");
        Objects.requireNonNull(doKeyword, "doKeyword must not be null");
        Objects.requireNonNull(blockStatement, "blockStatement must not be null");

        STNode stQueryActionNode = STNodeFactory.createQueryActionNode(
                queryPipeline.internalNode(),
                doKeyword.internalNode(),
                blockStatement.internalNode());
        return stQueryActionNode.createUnlinkedFacade();
    }

    public static OptionalFieldAccessExpressionNode createOptionalFieldAccessExpressionNode(
            ExpressionNode expression,
            Token optionalChainingToken,
            NameReferenceNode fieldName) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(optionalChainingToken, "optionalChainingToken must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");

        STNode stOptionalFieldAccessExpressionNode = STNodeFactory.createOptionalFieldAccessExpressionNode(
                expression.internalNode(),
                optionalChainingToken.internalNode(),
                fieldName.internalNode());
        return stOptionalFieldAccessExpressionNode.createUnlinkedFacade();
    }

    public static ConditionalExpressionNode createConditionalExpressionNode(
            ExpressionNode lhsExpression,
            Token questionMarkToken,
            ExpressionNode middleExpression,
            Token colonToken,
            ExpressionNode endExpression) {
        Objects.requireNonNull(lhsExpression, "lhsExpression must not be null");
        Objects.requireNonNull(questionMarkToken, "questionMarkToken must not be null");
        Objects.requireNonNull(middleExpression, "middleExpression must not be null");
        Objects.requireNonNull(colonToken, "colonToken must not be null");
        Objects.requireNonNull(endExpression, "endExpression must not be null");

        STNode stConditionalExpressionNode = STNodeFactory.createConditionalExpressionNode(
                lhsExpression.internalNode(),
                questionMarkToken.internalNode(),
                middleExpression.internalNode(),
                colonToken.internalNode(),
                endExpression.internalNode());
        return stConditionalExpressionNode.createUnlinkedFacade();
    }

    public static EnumDeclarationNode createEnumDeclarationNode(
            MetadataNode metadata,
            Token qualifier,
            Token enumKeywordToken,
            IdentifierToken identifier,
            Token openBraceToken,
            SeparatedNodeList<Node> enumMemberList,
            Token closeBraceToken) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(qualifier, "qualifier must not be null");
        Objects.requireNonNull(enumKeywordToken, "enumKeywordToken must not be null");
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
        Objects.requireNonNull(enumMemberList, "enumMemberList must not be null");
        Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");

        STNode stEnumDeclarationNode = STNodeFactory.createEnumDeclarationNode(
                metadata.internalNode(),
                qualifier.internalNode(),
                enumKeywordToken.internalNode(),
                identifier.internalNode(),
                openBraceToken.internalNode(),
                enumMemberList.underlyingListNode().internalNode(),
                closeBraceToken.internalNode());
        return stEnumDeclarationNode.createUnlinkedFacade();
    }

    public static EnumMemberNode createEnumMemberNode(
            MetadataNode metadata,
            IdentifierToken identifier,
            Token equalToken,
            ExpressionNode constExprNode) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(equalToken, "equalToken must not be null");
        Objects.requireNonNull(constExprNode, "constExprNode must not be null");

        STNode stEnumMemberNode = STNodeFactory.createEnumMemberNode(
                metadata.internalNode(),
                identifier.internalNode(),
                equalToken.internalNode(),
                constExprNode.internalNode());
        return stEnumMemberNode.createUnlinkedFacade();
    }

    public static ArrayTypeDescriptorNode createArrayTypeDescriptorNode(
            TypeDescriptorNode memberTypeDesc,
            Token openBracket,
            Node arrayLength,
            Token closeBracket) {
        Objects.requireNonNull(memberTypeDesc, "memberTypeDesc must not be null");
        Objects.requireNonNull(openBracket, "openBracket must not be null");
        Objects.requireNonNull(closeBracket, "closeBracket must not be null");

        STNode stArrayTypeDescriptorNode = STNodeFactory.createArrayTypeDescriptorNode(
                memberTypeDesc.internalNode(),
                openBracket.internalNode(),
                getOptionalSTNode(arrayLength),
                closeBracket.internalNode());
        return stArrayTypeDescriptorNode.createUnlinkedFacade();
    }

    public static TransactionStatementNode createTransactionStatementNode(
            Token transactionKeyword,
            BlockStatementNode blockStatement) {
        Objects.requireNonNull(transactionKeyword, "transactionKeyword must not be null");
        Objects.requireNonNull(blockStatement, "blockStatement must not be null");

        STNode stTransactionStatementNode = STNodeFactory.createTransactionStatementNode(
                transactionKeyword.internalNode(),
                blockStatement.internalNode());
        return stTransactionStatementNode.createUnlinkedFacade();
    }

    public static RollbackStatementNode createRollbackStatementNode(
            Token rollbackKeyword,
            ExpressionNode expression,
            Token semicolon) {
        Objects.requireNonNull(rollbackKeyword, "rollbackKeyword must not be null");
        Objects.requireNonNull(semicolon, "semicolon must not be null");

        STNode stRollbackStatementNode = STNodeFactory.createRollbackStatementNode(
                rollbackKeyword.internalNode(),
                getOptionalSTNode(expression),
                semicolon.internalNode());
        return stRollbackStatementNode.createUnlinkedFacade();
    }

    public static RetryStatementNode createRetryStatementNode(
            Token retryKeyword,
            TypeParameterNode typeParameter,
            ParenthesizedArgList arguments,
            StatementNode retryBody) {
        Objects.requireNonNull(retryKeyword, "retryKeyword must not be null");
        Objects.requireNonNull(retryBody, "retryBody must not be null");

        STNode stRetryStatementNode = STNodeFactory.createRetryStatementNode(
                retryKeyword.internalNode(),
                getOptionalSTNode(typeParameter),
                getOptionalSTNode(arguments),
                retryBody.internalNode());
        return stRetryStatementNode.createUnlinkedFacade();
    }

    public static CommitActionNode createCommitActionNode(
            Token commitKeyword) {
        Objects.requireNonNull(commitKeyword, "commitKeyword must not be null");

        STNode stCommitActionNode = STNodeFactory.createCommitActionNode(
                commitKeyword.internalNode());
        return stCommitActionNode.createUnlinkedFacade();
    }

    public static TransactionalExpressionNode createTransactionalExpressionNode(
            Token transactionalKeyword) {
        Objects.requireNonNull(transactionalKeyword, "transactionalKeyword must not be null");

        STNode stTransactionalExpressionNode = STNodeFactory.createTransactionalExpressionNode(
                transactionalKeyword.internalNode());
        return stTransactionalExpressionNode.createUnlinkedFacade();
    }

    public static ServiceConstructorExpressionNode createServiceConstructorExpressionNode(
            NodeList<AnnotationNode> annotations,
            Token serviceKeyword,
            Node serviceBody) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        Objects.requireNonNull(serviceKeyword, "serviceKeyword must not be null");
        Objects.requireNonNull(serviceBody, "serviceBody must not be null");

        STNode stServiceConstructorExpressionNode = STNodeFactory.createServiceConstructorExpressionNode(
                annotations.underlyingListNode().internalNode(),
                serviceKeyword.internalNode(),
                serviceBody.internalNode());
        return stServiceConstructorExpressionNode.createUnlinkedFacade();
    }

    public static ByteArrayLiteralNode createByteArrayLiteralNode(
            Token type,
            Token startBacktick,
            Token content,
            Token endBacktick) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(startBacktick, "startBacktick must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(endBacktick, "endBacktick must not be null");

        STNode stByteArrayLiteralNode = STNodeFactory.createByteArrayLiteralNode(
                type.internalNode(),
                startBacktick.internalNode(),
                content.internalNode(),
                endBacktick.internalNode());
        return stByteArrayLiteralNode.createUnlinkedFacade();
    }

    public static XMLFilterExpressionNode createXMLFilterExpressionNode(
            ExpressionNode expression,
            XMLNamePatternChainingNode xmlPatternChain) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(xmlPatternChain, "xmlPatternChain must not be null");

        STNode stXMLFilterExpressionNode = STNodeFactory.createXMLFilterExpressionNode(
                expression.internalNode(),
                xmlPatternChain.internalNode());
        return stXMLFilterExpressionNode.createUnlinkedFacade();
    }

    public static XMLStepExpressionNode createXMLStepExpressionNode(
            ExpressionNode expression,
            Node xmlStepStart) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(xmlStepStart, "xmlStepStart must not be null");

        STNode stXMLStepExpressionNode = STNodeFactory.createXMLStepExpressionNode(
                expression.internalNode(),
                xmlStepStart.internalNode());
        return stXMLStepExpressionNode.createUnlinkedFacade();
    }

    public static XMLNamePatternChainingNode createXMLNamePatternChainingNode(
            Token startToken,
            SeparatedNodeList<Node> xmlNamePattern,
            Token gtToken) {
        Objects.requireNonNull(startToken, "startToken must not be null");
        Objects.requireNonNull(xmlNamePattern, "xmlNamePattern must not be null");
        Objects.requireNonNull(gtToken, "gtToken must not be null");

        STNode stXMLNamePatternChainingNode = STNodeFactory.createXMLNamePatternChainingNode(
                startToken.internalNode(),
                xmlNamePattern.underlyingListNode().internalNode(),
                gtToken.internalNode());
        return stXMLNamePatternChainingNode.createUnlinkedFacade();
    }

    public static XMLAtomicNamePatternNode createXMLAtomicNamePatternNode(
            Token prefix,
            Token colon,
            Token name) {
        Objects.requireNonNull(prefix, "prefix must not be null");
        Objects.requireNonNull(colon, "colon must not be null");
        Objects.requireNonNull(name, "name must not be null");

        STNode stXMLAtomicNamePatternNode = STNodeFactory.createXMLAtomicNamePatternNode(
                prefix.internalNode(),
                colon.internalNode(),
                name.internalNode());
        return stXMLAtomicNamePatternNode.createUnlinkedFacade();
    }

    public static TypeReferenceTypeDescNode createTypeReferenceTypeDescNode(
            NameReferenceNode typeRef) {
        Objects.requireNonNull(typeRef, "typeRef must not be null");

        STNode stTypeReferenceTypeDescNode = STNodeFactory.createTypeReferenceTypeDescNode(
                typeRef.internalNode());
        return stTypeReferenceTypeDescNode.createUnlinkedFacade();
    }
}

