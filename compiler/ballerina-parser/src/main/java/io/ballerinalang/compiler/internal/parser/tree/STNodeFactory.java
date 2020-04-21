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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * A factory that constructs internal tree nodes.
 * <p>
 * This class contains various helper methods that create internal tree nodes.
 * <p>
 * Note that {@code STNodeFactory} must be used to create {@code STNode} instances. This approach allows
 * us to manage {@code STNode} production in the future. We could load nodes from a cache or add debug logs etc.
 *
 * This is a generated class.
 *
 * @since 1.3.0
 */
public class STNodeFactory extends STAbstractNodeFactory {

    private STNodeFactory() {
    }

    public static STNode createModulePartNode(
            STNode imports,
            STNode members,
            STNode eofToken) {

        return new STModulePartNode(
                imports,
                members,
                eofToken);
    }

    public static STNode createFunctionDefinitionNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode functionKeyword,
            STNode functionName,
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken,
            STNode returnTypeDesc,
            STNode functionBody) {

        return new STFunctionDefinitionNode(
                metadata,
                visibilityQualifier,
                functionKeyword,
                functionName,
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc,
                functionBody);
    }

    public static STNode createImportDeclarationNode(
            STNode importKeyword,
            STNode orgName,
            STNode moduleName,
            STNode version,
            STNode prefix,
            STNode semicolon) {

        return new STImportDeclarationNode(
                importKeyword,
                orgName,
                moduleName,
                version,
                prefix,
                semicolon);
    }

    public static STNode createListenerDeclarationNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode listenerKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STListenerDeclarationNode(
                metadata,
                visibilityQualifier,
                listenerKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public static STNode createTypeDefinitionNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode typeKeyword,
            STNode typeName,
            STNode typeDescriptor,
            STNode semicolonToken) {

        return new STTypeDefinitionNode(
                metadata,
                visibilityQualifier,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    public static STNode createServiceDeclarationNode(
            STNode metadata,
            STNode serviceKeyword,
            STNode serviceName,
            STNode onKeyword,
            STNode expressions,
            STNode serviceBody) {

        return new STServiceDeclarationNode(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody);
    }

    public static STNode createAssignmentStatementNode(
            STNode varRef,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {

        return new STAssignmentStatementNode(
                varRef,
                equalsToken,
                expression,
                semicolonToken);
    }

    public static STNode createCompoundAssignmentStatementNode(
            STNode lhsExpression,
            STNode binaryOperator,
            STNode equalsToken,
            STNode rhsExpression,
            STNode semicolonToken) {

        return new STCompoundAssignmentStatementNode(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken);
    }

    public static STNode createVariableDeclarationNode(
            STNode annotations,
            STNode finalKeyword,
            STNode typeName,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STVariableDeclarationNode(
                annotations,
                finalKeyword,
                typeName,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public static STNode createBlockStatementNode(
            STNode openBraceToken,
            STNode statements,
            STNode closeBraceToken) {

        return new STBlockStatementNode(
                openBraceToken,
                statements,
                closeBraceToken);
    }

    public static STNode createBreakStatementNode(
            STNode breakToken,
            STNode semicolonToken) {

        return new STBreakStatementNode(
                breakToken,
                semicolonToken);
    }

    public static STNode createExpressionStatementNode(
            SyntaxKind kind,
            STNode expression,
            STNode semicolonToken) {

        return new STExpressionStatementNode(
                kind,
                expression,
                semicolonToken);
    }

    public static STNode createContinueStatementNode(
            STNode continueToken,
            STNode semicolonToken) {

        return new STContinueStatementNode(
                continueToken,
                semicolonToken);
    }

    public static STNode createExternalFunctionBodyNode(
            STNode equalsToken,
            STNode annotations,
            STNode externalKeyword,
            STNode semicolonToken) {

        return new STExternalFunctionBodyNode(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken);
    }

    public static STNode createIfElseStatementNode(
            STNode ifKeyword,
            STNode condition,
            STNode ifBody,
            STNode elseBody) {

        return new STIfElseStatementNode(
                ifKeyword,
                condition,
                ifBody,
                elseBody);
    }

    public static STNode createElseBlockNode(
            STNode elseKeyword,
            STNode elseBody) {

        return new STElseBlockNode(
                elseKeyword,
                elseBody);
    }

    public static STNode createWhileStatementNode(
            STNode whileKeyword,
            STNode condition,
            STNode whileBody) {

        return new STWhileStatementNode(
                whileKeyword,
                condition,
                whileBody);
    }

    public static STNode createPanicStatementNode(
            STNode panicKeyword,
            STNode expression,
            STNode semicolonToken) {

        return new STPanicStatementNode(
                panicKeyword,
                expression,
                semicolonToken);
    }

    public static STNode createReturnStatementNode(
            STNode returnKeyword,
            STNode expression,
            STNode semicolonToken) {

        return new STReturnStatementNode(
                returnKeyword,
                expression,
                semicolonToken);
    }

    public static STNode createLocalTypeDefinitionStatementNode(
            STNode annotations,
            STNode typeKeyword,
            STNode typeName,
            STNode typeDescriptor,
            STNode semicolonToken) {

        return new STLocalTypeDefinitionStatementNode(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    public static STNode createBinaryExpressionNode(
            SyntaxKind kind,
            STNode lhsExpr,
            STNode operator,
            STNode rhsExpr) {

        return new STBinaryExpressionNode(
                kind,
                lhsExpr,
                operator,
                rhsExpr);
    }

    public static STNode createBracedExpressionNode(
            SyntaxKind kind,
            STNode openParen,
            STNode expression,
            STNode closeParen) {

        return new STBracedExpressionNode(
                kind,
                openParen,
                expression,
                closeParen);
    }

    public static STNode createCheckExpressionNode(
            SyntaxKind kind,
            STNode checkKeyword,
            STNode expression) {

        return new STCheckExpressionNode(
                kind,
                checkKeyword,
                expression);
    }

    public static STNode createFieldAccessExpressionNode(
            STNode expression,
            STNode dotToken,
            STNode fieldName) {

        return new STFieldAccessExpressionNode(
                expression,
                dotToken,
                fieldName);
    }

    public static STNode createFunctionCallExpressionNode(
            STNode functionName,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {

        return new STFunctionCallExpressionNode(
                functionName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public static STNode createMethodCallExpressionNode(
            STNode expression,
            STNode dotToken,
            STNode methodName,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {

        return new STMethodCallExpressionNode(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public static STNode createMappingConstructorExpressionNode(
            STNode openBrace,
            STNode fields,
            STNode closeBrace) {

        return new STMappingConstructorExpressionNode(
                openBrace,
                fields,
                closeBrace);
    }

    public static STNode createMemberAccessExpressionNode(
            STNode containerExpression,
            STNode openBracket,
            STNode keyExpression,
            STNode closeBracket) {

        return new STMemberAccessExpressionNode(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket);
    }

    public static STNode createTypeofExpressionNode(
            STNode typeofKeyword,
            STNode expression) {

        return new STTypeofExpressionNode(
                typeofKeyword,
                expression);
    }

    public static STNode createUnaryExpressionNode(
            STNode unaryOperator,
            STNode expression) {

        return new STUnaryExpressionNode(
                unaryOperator,
                expression);
    }

    public static STNode createComputedNameFieldNode(
            STNode leadingComma,
            STNode openBracket,
            STNode fieldNameExpr,
            STNode closeBracket,
            STNode colonToken,
            STNode valueExpr) {

        return new STComputedNameFieldNode(
                leadingComma,
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr);
    }

    public static STNode createConstantDeclarationNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode constKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STConstantDeclarationNode(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public static STNode createDefaultableParameterNode(
            STNode leadingComma,
            STNode annotations,
            STNode visibilityQualifier,
            STNode typeName,
            STNode paramName,
            STNode equalsToken,
            STNode expression) {

        return new STDefaultableParameterNode(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName,
                equalsToken,
                expression);
    }

    public static STNode createRequiredParameterNode(
            STNode leadingComma,
            STNode annotations,
            STNode visibilityQualifier,
            STNode typeName,
            STNode paramName) {

        return new STRequiredParameterNode(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName);
    }

    public static STNode createRestParameterNode(
            STNode leadingComma,
            STNode annotations,
            STNode typeName,
            STNode ellipsisToken,
            STNode paramName) {

        return new STRestParameterNode(
                leadingComma,
                annotations,
                typeName,
                ellipsisToken,
                paramName);
    }

    public static STNode createExpressionListItemNode(
            STNode leadingComma,
            STNode expression) {

        return new STExpressionListItemNode(
                leadingComma,
                expression);
    }

    public static STNode createImportOrgNameNode(
            STNode orgName,
            STNode slashToken) {

        return new STImportOrgNameNode(
                orgName,
                slashToken);
    }

    public static STNode createImportPrefixNode(
            STNode asKeyword,
            STNode prefix) {

        return new STImportPrefixNode(
                asKeyword,
                prefix);
    }

    public static STNode createImportSubVersionNode(
            STNode leadingDot,
            STNode versionNumber) {

        return new STImportSubVersionNode(
                leadingDot,
                versionNumber);
    }

    public static STNode createImportVersionNode(
            STNode versionKeyword,
            STNode versionNumber) {

        return new STImportVersionNode(
                versionKeyword,
                versionNumber);
    }

    public static STNode createSpecificFieldNode(
            STNode leadingComma,
            STNode fieldName,
            STNode colon,
            STNode valueExpr) {

        return new STSpecificFieldNode(
                leadingComma,
                fieldName,
                colon,
                valueExpr);
    }

    public static STNode createSpreadFieldNode(
            STNode leadingComma,
            STNode ellipsis,
            STNode valueExpr) {

        return new STSpreadFieldNode(
                leadingComma,
                ellipsis,
                valueExpr);
    }

    public static STNode createNamedArgumentNode(
            STNode leadingComma,
            STNode argumentName,
            STNode equalsToken,
            STNode expression) {

        return new STNamedArgumentNode(
                leadingComma,
                argumentName,
                equalsToken,
                expression);
    }

    public static STNode createPositionalArgumentNode(
            STNode leadingComma,
            STNode expression) {

        return new STPositionalArgumentNode(
                leadingComma,
                expression);
    }

    public static STNode createRestArgumentNode(
            STNode leadingComma,
            STNode ellipsis,
            STNode expression) {

        return new STRestArgumentNode(
                leadingComma,
                ellipsis,
                expression);
    }

    public static STNode createObjectTypeDescriptorNode(
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode openBrace,
            STNode members,
            STNode closeBrace) {

        return new STObjectTypeDescriptorNode(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    public static STNode createRecordTypeDescriptorNode(
            STNode objectKeyword,
            STNode bodyStartDelimiter,
            STNode fields,
            STNode bodyEndDelimiter) {

        return new STRecordTypeDescriptorNode(
                objectKeyword,
                bodyStartDelimiter,
                fields,
                bodyEndDelimiter);
    }

    public static STNode createReturnTypeDescriptorNode(
            STNode returnsKeyword,
            STNode annotations,
            STNode type) {

        return new STReturnTypeDescriptorNode(
                returnsKeyword,
                annotations,
                type);
    }

    public static STNode createNilTypeDescriptorNode(
            STNode openParenToken,
            STNode closeParenToken) {

        return new STNilTypeDescriptorNode(
                openParenToken,
                closeParenToken);
    }

    public static STNode createOptionalTypeDescriptorNode(
            STNode typeDescriptor,
            STNode questionMarkToken) {

        return new STOptionalTypeDescriptorNode(
                typeDescriptor,
                questionMarkToken);
    }

    public static STNode createObjectFieldNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {

        return new STObjectFieldNode(
                metadata,
                visibilityQualifier,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    public static STNode createRecordFieldNode(
            STNode metadata,
            STNode typeName,
            STNode fieldName,
            STNode questionMarkToken,
            STNode semicolonToken) {

        return new STRecordFieldNode(
                metadata,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    public static STNode createRecordFieldWithDefaultValueNode(
            STNode metadata,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {

        return new STRecordFieldWithDefaultValueNode(
                metadata,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    public static STNode createRecordRestDescriptorNode(
            STNode typeName,
            STNode ellipsisToken,
            STNode semicolonToken) {

        return new STRecordRestDescriptorNode(
                typeName,
                ellipsisToken,
                semicolonToken);
    }

    public static STNode createTypeReferenceNode(
            STNode asteriskToken,
            STNode typeName,
            STNode semicolonToken) {

        return new STTypeReferenceNode(
                asteriskToken,
                typeName,
                semicolonToken);
    }

    public static STNode createQualifiedIdentifierNode(
            STNode modulePrefix,
            STNode colon,
            STNode identifier) {

        return new STQualifiedIdentifierNode(
                modulePrefix,
                colon,
                identifier);
    }

    public static STNode createServiceBodyNode(
            STNode openBraceToken,
            STNode resources,
            STNode closeBraceToken) {

        return new STServiceBodyNode(
                openBraceToken,
                resources,
                closeBraceToken);
    }

    public static STNode createAnnotationNode(
            STNode atToken,
            STNode annotReference,
            STNode annotValue) {

        return new STAnnotationNode(
                atToken,
                annotReference,
                annotValue);
    }

    public static STNode createMetadataNode(
            STNode documentationString,
            STNode annotations) {

        return new STMetadataNode(
                documentationString,
                annotations);
    }

    public static STNode createModuleVariableDeclarationNode(
            STNode metadata,
            STNode finalKeyword,
            STNode typeName,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STModuleVariableDeclarationNode(
                metadata,
                finalKeyword,
                typeName,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public static STNode createTypeTestExpressionNode(
            STNode expression,
            STNode isKeyword,
            STNode typeDescriptor) {

        return new STTypeTestExpressionNode(
                expression,
                isKeyword,
                typeDescriptor);
    }

    public static STNode createArrayTypeDescriptorNode(
            STNode typeDescriptorNode,
            STNode openBracketToken,
            STNode arrayLengthNode,
            STNode closeBracketToken) {

        return new STArrayTypeDescriptorNode(
                typeDescriptorNode,
                openBracketToken,
                arrayLengthNode,
                closeBracketToken);
    }

    public static STNode createRemoteMethodCallActionNode(
            STNode expression,
            STNode rightArrowToken,
            STNode methodName,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {

        return new STRemoteMethodCallActionNode(
                expression,
                rightArrowToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public static STNode createNilLiteralNode(
            STNode openParenToken,
            STNode closeParenToken) {

        return new STNilLiteralNode(
                openParenToken,
                closeParenToken);
    }

    public static STNode createAnnotationDeclarationNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode constKeyword,
            STNode annotationKeyword,
            STNode typeDescriptor,
            STNode annotationTag,
            STNode onKeyword,
            STNode attachPoints,
            STNode semicolonToken) {

        return new STAnnotationDeclarationNode(
                metadata,
                visibilityQualifier,
                constKeyword,
                annotationKeyword,
                typeDescriptor,
                annotationTag,
                onKeyword,
                attachPoints,
                semicolonToken);
    }

    public static STNode createAnnotationAttachPointNode(
            STNode sourceKeyword,
            STNode firstIdent,
            STNode secondIdent) {

        return new STAnnotationAttachPointNode(
                sourceKeyword,
                firstIdent,
                secondIdent);
    }

    public static STNode createXMLNamespaceDeclarationNode(
            STNode xmlnsKeyword,
            STNode namespaceuri,
            STNode asKeyword,
            STNode namespacePrefix,
            STNode semicolonToken) {

        return new STXMLNamespaceDeclarationNode(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken);
    }

    public static STNode createFunctionBodyBlockNode(
            STNode openBraceToken,
            STNode namedWorkerDeclarator,
            STNode statements,
            STNode closeBraceToken) {

        return new STFunctionBodyBlockNode(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken);
    }

    public static STNode createNamedWorkerDeclarationNode(
            STNode annotations,
            STNode workerKeyword,
            STNode workerName,
            STNode returnTypeDesc,
            STNode workerBody) {

        return new STNamedWorkerDeclarationNode(
                annotations,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody);
    }

    public static STNode createNamedWorkerDeclarator(
            STNode workerInitStatements,
            STNode namedWorkerDeclarations) {

        return new STNamedWorkerDeclarator(
                workerInitStatements,
                namedWorkerDeclarations);
    }

    public static STNode createDocumentationStringNode(
            STNode documentationLines) {

        return new STDocumentationStringNode(
                documentationLines);
    }
}

