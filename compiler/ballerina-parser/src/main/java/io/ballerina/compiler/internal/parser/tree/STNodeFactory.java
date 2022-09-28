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
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.SyntaxKind;

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
 * @since 2.0.0
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
            SyntaxKind kind,
            STNode metadata,
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionName,
            STNode relativeResourcePath,
            STNode functionSignature,
            STNode functionBody) {

        return new STFunctionDefinitionNode(
                kind,
                metadata,
                qualifierList,
                functionKeyword,
                functionName,
                relativeResourcePath,
                functionSignature,
                functionBody);
    }

    public static STNode createImportDeclarationNode(
            STNode importKeyword,
            STNode orgName,
            STNode moduleName,
            STNode prefix,
            STNode semicolon) {

        return new STImportDeclarationNode(
                importKeyword,
                orgName,
                moduleName,
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
            STNode qualifiers,
            STNode serviceKeyword,
            STNode typeDescriptor,
            STNode absoluteResourcePath,
            STNode onKeyword,
            STNode expressions,
            STNode openBraceToken,
            STNode members,
            STNode closeBraceToken,
            STNode semicolonToken) {

        return new STServiceDeclarationNode(
                metadata,
                qualifiers,
                serviceKeyword,
                typeDescriptor,
                absoluteResourcePath,
                onKeyword,
                expressions,
                openBraceToken,
                members,
                closeBraceToken,
                semicolonToken);
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
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STVariableDeclarationNode(
                annotations,
                finalKeyword,
                typedBindingPattern,
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

    public static STNode createFailStatementNode(
            STNode failKeyword,
            STNode expression,
            STNode semicolonToken) {

        return new STFailStatementNode(
                failKeyword,
                expression,
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
            STNode whileBody,
            STNode onFailClause) {

        return new STWhileStatementNode(
                whileKeyword,
                condition,
                whileBody,
                onFailClause);
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

    public static STNode createLockStatementNode(
            STNode lockKeyword,
            STNode blockStatement,
            STNode onFailClause) {

        return new STLockStatementNode(
                lockKeyword,
                blockStatement,
                onFailClause);
    }

    public static STNode createForkStatementNode(
            STNode forkKeyword,
            STNode openBraceToken,
            STNode namedWorkerDeclarations,
            STNode closeBraceToken) {

        return new STForkStatementNode(
                forkKeyword,
                openBraceToken,
                namedWorkerDeclarations,
                closeBraceToken);
    }

    public static STNode createForEachStatementNode(
            STNode forEachKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode actionOrExpressionNode,
            STNode blockStatement,
            STNode onFailClause) {

        return new STForEachStatementNode(
                forEachKeyword,
                typedBindingPattern,
                inKeyword,
                actionOrExpressionNode,
                blockStatement,
                onFailClause);
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

    public static STNode createIndexedExpressionNode(
            STNode containerExpression,
            STNode openBracket,
            STNode keyExpression,
            STNode closeBracket) {

        return new STIndexedExpressionNode(
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
            STNode openBracket,
            STNode fieldNameExpr,
            STNode closeBracket,
            STNode colonToken,
            STNode valueExpr) {

        return new STComputedNameFieldNode(
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
            STNode annotations,
            STNode typeName,
            STNode paramName,
            STNode equalsToken,
            STNode expression) {

        return new STDefaultableParameterNode(
                annotations,
                typeName,
                paramName,
                equalsToken,
                expression);
    }

    public static STNode createRequiredParameterNode(
            STNode annotations,
            STNode typeName,
            STNode paramName) {

        return new STRequiredParameterNode(
                annotations,
                typeName,
                paramName);
    }

    public static STNode createIncludedRecordParameterNode(
            STNode annotations,
            STNode asteriskToken,
            STNode typeName,
            STNode paramName) {

        return new STIncludedRecordParameterNode(
                annotations,
                asteriskToken,
                typeName,
                paramName);
    }

    public static STNode createRestParameterNode(
            STNode annotations,
            STNode typeName,
            STNode ellipsisToken,
            STNode paramName) {

        return new STRestParameterNode(
                annotations,
                typeName,
                ellipsisToken,
                paramName);
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

    public static STNode createSpecificFieldNode(
            STNode readonlyKeyword,
            STNode fieldName,
            STNode colon,
            STNode valueExpr) {

        return new STSpecificFieldNode(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr);
    }

    public static STNode createSpreadFieldNode(
            STNode ellipsis,
            STNode valueExpr) {

        return new STSpreadFieldNode(
                ellipsis,
                valueExpr);
    }

    public static STNode createNamedArgumentNode(
            STNode argumentName,
            STNode equalsToken,
            STNode expression) {

        return new STNamedArgumentNode(
                argumentName,
                equalsToken,
                expression);
    }

    public static STNode createPositionalArgumentNode(
            STNode expression) {

        return new STPositionalArgumentNode(
                expression);
    }

    public static STNode createRestArgumentNode(
            STNode ellipsis,
            STNode expression) {

        return new STRestArgumentNode(
                ellipsis,
                expression);
    }

    public static STNode createInferredTypedescDefaultNode(
            STNode ltToken,
            STNode gtToken) {

        return new STInferredTypedescDefaultNode(
                ltToken,
                gtToken);
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

    public static STNode createObjectConstructorExpressionNode(
            STNode annotations,
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode typeReference,
            STNode openBraceToken,
            STNode members,
            STNode closeBraceToken) {

        return new STObjectConstructorExpressionNode(
                annotations,
                objectTypeQualifiers,
                objectKeyword,
                typeReference,
                openBraceToken,
                members,
                closeBraceToken);
    }

    public static STNode createRecordTypeDescriptorNode(
            STNode recordKeyword,
            STNode bodyStartDelimiter,
            STNode fields,
            STNode recordRestDescriptor,
            STNode bodyEndDelimiter) {

        return new STRecordTypeDescriptorNode(
                recordKeyword,
                bodyStartDelimiter,
                fields,
                recordRestDescriptor,
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
            STNode qualifierList,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {

        return new STObjectFieldNode(
                metadata,
                visibilityQualifier,
                qualifierList,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    public static STNode createRecordFieldNode(
            STNode metadata,
            STNode readonlyKeyword,
            STNode typeName,
            STNode fieldName,
            STNode questionMarkToken,
            STNode semicolonToken) {

        return new STRecordFieldNode(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    public static STNode createRecordFieldWithDefaultValueNode(
            STNode metadata,
            STNode readonlyKeyword,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {

        return new STRecordFieldWithDefaultValueNode(
                metadata,
                readonlyKeyword,
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
            STNode visibilityQualifier,
            STNode qualifiers,
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STModuleVariableDeclarationNode(
                metadata,
                visibilityQualifier,
                qualifiers,
                typedBindingPattern,
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

    public static STNode createMapTypeDescriptorNode(
            STNode mapKeywordToken,
            STNode mapTypeParamsNode) {

        return new STMapTypeDescriptorNode(
                mapKeywordToken,
                mapTypeParamsNode);
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
            STNode identifiers) {

        return new STAnnotationAttachPointNode(
                sourceKeyword,
                identifiers);
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

    public static STNode createModuleXMLNamespaceDeclarationNode(
            STNode xmlnsKeyword,
            STNode namespaceuri,
            STNode asKeyword,
            STNode namespacePrefix,
            STNode semicolonToken) {

        return new STModuleXMLNamespaceDeclarationNode(
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
            STNode closeBraceToken,
            STNode semicolonToken) {

        return new STFunctionBodyBlockNode(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken,
                semicolonToken);
    }

    public static STNode createNamedWorkerDeclarationNode(
            STNode annotations,
            STNode transactionalKeyword,
            STNode workerKeyword,
            STNode workerName,
            STNode returnTypeDesc,
            STNode workerBody) {

        return new STNamedWorkerDeclarationNode(
                annotations,
                transactionalKeyword,
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

    public static STNode createBasicLiteralNode(
            SyntaxKind kind,
            STNode literalToken) {

        return new STBasicLiteralNode(
                kind,
                literalToken);
    }

    public static STNode createSimpleNameReferenceNode(
            STNode name) {

        return new STSimpleNameReferenceNode(
                name);
    }

    public static STNode createQualifiedNameReferenceNode(
            STNode modulePrefix,
            STNode colon,
            STNode identifier) {

        return new STQualifiedNameReferenceNode(
                modulePrefix,
                colon,
                identifier);
    }

    public static STNode createBuiltinSimpleNameReferenceNode(
            SyntaxKind kind,
            STNode name) {

        return new STBuiltinSimpleNameReferenceNode(
                kind,
                name);
    }

    public static STNode createTrapExpressionNode(
            SyntaxKind kind,
            STNode trapKeyword,
            STNode expression) {

        return new STTrapExpressionNode(
                kind,
                trapKeyword,
                expression);
    }

    public static STNode createListConstructorExpressionNode(
            STNode openBracket,
            STNode expressions,
            STNode closeBracket) {

        return new STListConstructorExpressionNode(
                openBracket,
                expressions,
                closeBracket);
    }

    public static STNode createTypeCastExpressionNode(
            STNode ltToken,
            STNode typeCastParam,
            STNode gtToken,
            STNode expression) {

        return new STTypeCastExpressionNode(
                ltToken,
                typeCastParam,
                gtToken,
                expression);
    }

    public static STNode createTypeCastParamNode(
            STNode annotations,
            STNode type) {

        return new STTypeCastParamNode(
                annotations,
                type);
    }

    public static STNode createUnionTypeDescriptorNode(
            STNode leftTypeDesc,
            STNode pipeToken,
            STNode rightTypeDesc) {

        return new STUnionTypeDescriptorNode(
                leftTypeDesc,
                pipeToken,
                rightTypeDesc);
    }

    public static STNode createTableConstructorExpressionNode(
            STNode tableKeyword,
            STNode keySpecifier,
            STNode openBracket,
            STNode rows,
            STNode closeBracket) {

        return new STTableConstructorExpressionNode(
                tableKeyword,
                keySpecifier,
                openBracket,
                rows,
                closeBracket);
    }

    public static STNode createKeySpecifierNode(
            STNode keyKeyword,
            STNode openParenToken,
            STNode fieldNames,
            STNode closeParenToken) {

        return new STKeySpecifierNode(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken);
    }

    public static STNode createStreamTypeDescriptorNode(
            STNode streamKeywordToken,
            STNode streamTypeParamsNode) {

        return new STStreamTypeDescriptorNode(
                streamKeywordToken,
                streamTypeParamsNode);
    }

    public static STNode createStreamTypeParamsNode(
            STNode ltToken,
            STNode leftTypeDescNode,
            STNode commaToken,
            STNode rightTypeDescNode,
            STNode gtToken) {

        return new STStreamTypeParamsNode(
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken);
    }

    public static STNode createLetExpressionNode(
            STNode letKeyword,
            STNode letVarDeclarations,
            STNode inKeyword,
            STNode expression) {

        return new STLetExpressionNode(
                letKeyword,
                letVarDeclarations,
                inKeyword,
                expression);
    }

    public static STNode createLetVariableDeclarationNode(
            STNode annotations,
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode expression) {

        return new STLetVariableDeclarationNode(
                annotations,
                typedBindingPattern,
                equalsToken,
                expression);
    }

    public static STNode createTemplateExpressionNode(
            SyntaxKind kind,
            STNode type,
            STNode startBacktick,
            STNode content,
            STNode endBacktick) {

        return new STTemplateExpressionNode(
                kind,
                type,
                startBacktick,
                content,
                endBacktick);
    }

    public static STNode createXMLElementNode(
            STNode startTag,
            STNode content,
            STNode endTag) {

        return new STXMLElementNode(
                startTag,
                content,
                endTag);
    }

    public static STNode createXMLStartTagNode(
            STNode ltToken,
            STNode name,
            STNode attributes,
            STNode getToken) {

        return new STXMLStartTagNode(
                ltToken,
                name,
                attributes,
                getToken);
    }

    public static STNode createXMLEndTagNode(
            STNode ltToken,
            STNode slashToken,
            STNode name,
            STNode getToken) {

        return new STXMLEndTagNode(
                ltToken,
                slashToken,
                name,
                getToken);
    }

    public static STNode createXMLSimpleNameNode(
            STNode name) {

        return new STXMLSimpleNameNode(
                name);
    }

    public static STNode createXMLQualifiedNameNode(
            STNode prefix,
            STNode colon,
            STNode name) {

        return new STXMLQualifiedNameNode(
                prefix,
                colon,
                name);
    }

    public static STNode createXMLEmptyElementNode(
            STNode ltToken,
            STNode name,
            STNode attributes,
            STNode slashToken,
            STNode getToken) {

        return new STXMLEmptyElementNode(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken);
    }

    public static STNode createInterpolationNode(
            STNode interpolationStartToken,
            STNode expression,
            STNode interpolationEndToken) {

        return new STInterpolationNode(
                interpolationStartToken,
                expression,
                interpolationEndToken);
    }

    public static STNode createXMLTextNode(
            STNode content) {

        return new STXMLTextNode(
                content);
    }

    public static STNode createXMLAttributeNode(
            STNode attributeName,
            STNode equalToken,
            STNode value) {

        return new STXMLAttributeNode(
                attributeName,
                equalToken,
                value);
    }

    public static STNode createXMLAttributeValue(
            STNode startQuote,
            STNode value,
            STNode endQuote) {

        return new STXMLAttributeValue(
                startQuote,
                value,
                endQuote);
    }

    public static STNode createXMLComment(
            STNode commentStart,
            STNode content,
            STNode commentEnd) {

        return new STXMLComment(
                commentStart,
                content,
                commentEnd);
    }

    public static STNode createXMLCDATANode(
            STNode cdataStart,
            STNode content,
            STNode cdataEnd) {

        return new STXMLCDATANode(
                cdataStart,
                content,
                cdataEnd);
    }

    public static STNode createXMLProcessingInstruction(
            STNode piStart,
            STNode target,
            STNode data,
            STNode piEnd) {

        return new STXMLProcessingInstruction(
                piStart,
                target,
                data,
                piEnd);
    }

    public static STNode createTableTypeDescriptorNode(
            STNode tableKeywordToken,
            STNode rowTypeParameterNode,
            STNode keyConstraintNode) {

        return new STTableTypeDescriptorNode(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode);
    }

    public static STNode createTypeParameterNode(
            STNode ltToken,
            STNode typeNode,
            STNode gtToken) {

        return new STTypeParameterNode(
                ltToken,
                typeNode,
                gtToken);
    }

    public static STNode createKeyTypeConstraintNode(
            STNode keyKeywordToken,
            STNode typeParameterNode) {

        return new STKeyTypeConstraintNode(
                keyKeywordToken,
                typeParameterNode);
    }

    public static STNode createFunctionTypeDescriptorNode(
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionSignature) {

        return new STFunctionTypeDescriptorNode(
                qualifierList,
                functionKeyword,
                functionSignature);
    }

    public static STNode createFunctionSignatureNode(
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken,
            STNode returnTypeDesc) {

        return new STFunctionSignatureNode(
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc);
    }

    public static STNode createExplicitAnonymousFunctionExpressionNode(
            STNode annotations,
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionSignature,
            STNode functionBody) {

        return new STExplicitAnonymousFunctionExpressionNode(
                annotations,
                qualifierList,
                functionKeyword,
                functionSignature,
                functionBody);
    }

    public static STNode createExpressionFunctionBodyNode(
            STNode rightDoubleArrow,
            STNode expression,
            STNode semicolon) {

        return new STExpressionFunctionBodyNode(
                rightDoubleArrow,
                expression,
                semicolon);
    }

    public static STNode createTupleTypeDescriptorNode(
            STNode openBracketToken,
            STNode memberTypeDesc,
            STNode closeBracketToken) {

        return new STTupleTypeDescriptorNode(
                openBracketToken,
                memberTypeDesc,
                closeBracketToken);
    }

    public static STNode createParenthesisedTypeDescriptorNode(
            STNode openParenToken,
            STNode typedesc,
            STNode closeParenToken) {

        return new STParenthesisedTypeDescriptorNode(
                openParenToken,
                typedesc,
                closeParenToken);
    }

    public static STNode createExplicitNewExpressionNode(
            STNode newKeyword,
            STNode typeDescriptor,
            STNode parenthesizedArgList) {

        return new STExplicitNewExpressionNode(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList);
    }

    public static STNode createImplicitNewExpressionNode(
            STNode newKeyword,
            STNode parenthesizedArgList) {

        return new STImplicitNewExpressionNode(
                newKeyword,
                parenthesizedArgList);
    }

    public static STNode createParenthesizedArgList(
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {

        return new STParenthesizedArgList(
                openParenToken,
                arguments,
                closeParenToken);
    }

    public static STNode createQueryConstructTypeNode(
            STNode keyword,
            STNode keySpecifier) {

        return new STQueryConstructTypeNode(
                keyword,
                keySpecifier);
    }

    public static STNode createFromClauseNode(
            STNode fromKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode expression) {

        return new STFromClauseNode(
                fromKeyword,
                typedBindingPattern,
                inKeyword,
                expression);
    }

    public static STNode createWhereClauseNode(
            STNode whereKeyword,
            STNode expression) {

        return new STWhereClauseNode(
                whereKeyword,
                expression);
    }

    public static STNode createLetClauseNode(
            STNode letKeyword,
            STNode letVarDeclarations) {

        return new STLetClauseNode(
                letKeyword,
                letVarDeclarations);
    }

    public static STNode createJoinClauseNode(
            STNode outerKeyword,
            STNode joinKeyword,
            STNode typedBindingPattern,
            STNode inKeyword,
            STNode expression,
            STNode joinOnCondition) {

        return new STJoinClauseNode(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition);
    }

    public static STNode createOnClauseNode(
            STNode onKeyword,
            STNode lhsExpression,
            STNode equalsKeyword,
            STNode rhsExpression) {

        return new STOnClauseNode(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression);
    }

    public static STNode createLimitClauseNode(
            STNode limitKeyword,
            STNode expression) {

        return new STLimitClauseNode(
                limitKeyword,
                expression);
    }

    public static STNode createOnConflictClauseNode(
            STNode onKeyword,
            STNode conflictKeyword,
            STNode expression) {

        return new STOnConflictClauseNode(
                onKeyword,
                conflictKeyword,
                expression);
    }

    public static STNode createQueryPipelineNode(
            STNode fromClause,
            STNode intermediateClauses) {

        return new STQueryPipelineNode(
                fromClause,
                intermediateClauses);
    }

    public static STNode createSelectClauseNode(
            STNode selectKeyword,
            STNode expression) {

        return new STSelectClauseNode(
                selectKeyword,
                expression);
    }

    public static STNode createQueryExpressionNode(
            STNode queryConstructType,
            STNode queryPipeline,
            STNode selectClause,
            STNode onConflictClause) {

        return new STQueryExpressionNode(
                queryConstructType,
                queryPipeline,
                selectClause,
                onConflictClause);
    }

    public static STNode createQueryActionNode(
            STNode queryPipeline,
            STNode doKeyword,
            STNode blockStatement) {

        return new STQueryActionNode(
                queryPipeline,
                doKeyword,
                blockStatement);
    }

    public static STNode createIntersectionTypeDescriptorNode(
            STNode leftTypeDesc,
            STNode bitwiseAndToken,
            STNode rightTypeDesc) {

        return new STIntersectionTypeDescriptorNode(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc);
    }

    public static STNode createImplicitAnonymousFunctionParameters(
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken) {

        return new STImplicitAnonymousFunctionParameters(
                openParenToken,
                parameters,
                closeParenToken);
    }

    public static STNode createImplicitAnonymousFunctionExpressionNode(
            STNode params,
            STNode rightDoubleArrow,
            STNode expression) {

        return new STImplicitAnonymousFunctionExpressionNode(
                params,
                rightDoubleArrow,
                expression);
    }

    public static STNode createStartActionNode(
            STNode annotations,
            STNode startKeyword,
            STNode expression) {

        return new STStartActionNode(
                annotations,
                startKeyword,
                expression);
    }

    public static STNode createFlushActionNode(
            STNode flushKeyword,
            STNode peerWorker) {

        return new STFlushActionNode(
                flushKeyword,
                peerWorker);
    }

    public static STNode createSingletonTypeDescriptorNode(
            STNode simpleContExprNode) {

        return new STSingletonTypeDescriptorNode(
                simpleContExprNode);
    }

    public static STNode createMethodDeclarationNode(
            SyntaxKind kind,
            STNode metadata,
            STNode qualifierList,
            STNode functionKeyword,
            STNode methodName,
            STNode relativeResourcePath,
            STNode methodSignature,
            STNode semicolon) {

        return new STMethodDeclarationNode(
                kind,
                metadata,
                qualifierList,
                functionKeyword,
                methodName,
                relativeResourcePath,
                methodSignature,
                semicolon);
    }

    public static STNode createTypedBindingPatternNode(
            STNode typeDescriptor,
            STNode bindingPattern) {

        return new STTypedBindingPatternNode(
                typeDescriptor,
                bindingPattern);
    }

    public static STNode createCaptureBindingPatternNode(
            STNode variableName) {

        return new STCaptureBindingPatternNode(
                variableName);
    }

    public static STNode createWildcardBindingPatternNode(
            STNode underscoreToken) {

        return new STWildcardBindingPatternNode(
                underscoreToken);
    }

    public static STNode createListBindingPatternNode(
            STNode openBracket,
            STNode bindingPatterns,
            STNode closeBracket) {

        return new STListBindingPatternNode(
                openBracket,
                bindingPatterns,
                closeBracket);
    }

    public static STNode createMappingBindingPatternNode(
            STNode openBrace,
            STNode fieldBindingPatterns,
            STNode closeBrace) {

        return new STMappingBindingPatternNode(
                openBrace,
                fieldBindingPatterns,
                closeBrace);
    }

    public static STNode createFieldBindingPatternFullNode(
            STNode variableName,
            STNode colon,
            STNode bindingPattern) {

        return new STFieldBindingPatternFullNode(
                variableName,
                colon,
                bindingPattern);
    }

    public static STNode createFieldBindingPatternVarnameNode(
            STNode variableName) {

        return new STFieldBindingPatternVarnameNode(
                variableName);
    }

    public static STNode createRestBindingPatternNode(
            STNode ellipsisToken,
            STNode variableName) {

        return new STRestBindingPatternNode(
                ellipsisToken,
                variableName);
    }

    public static STNode createErrorBindingPatternNode(
            STNode errorKeyword,
            STNode typeReference,
            STNode openParenthesis,
            STNode argListBindingPatterns,
            STNode closeParenthesis) {

        return new STErrorBindingPatternNode(
                errorKeyword,
                typeReference,
                openParenthesis,
                argListBindingPatterns,
                closeParenthesis);
    }

    public static STNode createNamedArgBindingPatternNode(
            STNode argName,
            STNode equalsToken,
            STNode bindingPattern) {

        return new STNamedArgBindingPatternNode(
                argName,
                equalsToken,
                bindingPattern);
    }

    public static STNode createAsyncSendActionNode(
            STNode expression,
            STNode rightArrowToken,
            STNode peerWorker) {

        return new STAsyncSendActionNode(
                expression,
                rightArrowToken,
                peerWorker);
    }

    public static STNode createSyncSendActionNode(
            STNode expression,
            STNode syncSendToken,
            STNode peerWorker) {

        return new STSyncSendActionNode(
                expression,
                syncSendToken,
                peerWorker);
    }

    public static STNode createReceiveActionNode(
            STNode leftArrow,
            STNode receiveWorkers) {

        return new STReceiveActionNode(
                leftArrow,
                receiveWorkers);
    }

    public static STNode createReceiveFieldsNode(
            STNode openBrace,
            STNode receiveFields,
            STNode closeBrace) {

        return new STReceiveFieldsNode(
                openBrace,
                receiveFields,
                closeBrace);
    }

    public static STNode createRestDescriptorNode(
            STNode typeDescriptor,
            STNode ellipsisToken) {

        return new STRestDescriptorNode(
                typeDescriptor,
                ellipsisToken);
    }

    public static STNode createDoubleGTTokenNode(
            STNode openGTToken,
            STNode endGTToken) {

        return new STDoubleGTTokenNode(
                openGTToken,
                endGTToken);
    }

    public static STNode createTrippleGTTokenNode(
            STNode openGTToken,
            STNode middleGTToken,
            STNode endGTToken) {

        return new STTrippleGTTokenNode(
                openGTToken,
                middleGTToken,
                endGTToken);
    }

    public static STNode createWaitActionNode(
            STNode waitKeyword,
            STNode waitFutureExpr) {

        return new STWaitActionNode(
                waitKeyword,
                waitFutureExpr);
    }

    public static STNode createWaitFieldsListNode(
            STNode openBrace,
            STNode waitFields,
            STNode closeBrace) {

        return new STWaitFieldsListNode(
                openBrace,
                waitFields,
                closeBrace);
    }

    public static STNode createWaitFieldNode(
            STNode fieldName,
            STNode colon,
            STNode waitFutureExpr) {

        return new STWaitFieldNode(
                fieldName,
                colon,
                waitFutureExpr);
    }

    public static STNode createAnnotAccessExpressionNode(
            STNode expression,
            STNode annotChainingToken,
            STNode annotTagReference) {

        return new STAnnotAccessExpressionNode(
                expression,
                annotChainingToken,
                annotTagReference);
    }

    public static STNode createOptionalFieldAccessExpressionNode(
            STNode expression,
            STNode optionalChainingToken,
            STNode fieldName) {

        return new STOptionalFieldAccessExpressionNode(
                expression,
                optionalChainingToken,
                fieldName);
    }

    public static STNode createConditionalExpressionNode(
            STNode lhsExpression,
            STNode questionMarkToken,
            STNode middleExpression,
            STNode colonToken,
            STNode endExpression) {

        return new STConditionalExpressionNode(
                lhsExpression,
                questionMarkToken,
                middleExpression,
                colonToken,
                endExpression);
    }

    public static STNode createEnumDeclarationNode(
            STNode metadata,
            STNode qualifier,
            STNode enumKeywordToken,
            STNode identifier,
            STNode openBraceToken,
            STNode enumMemberList,
            STNode closeBraceToken,
            STNode semicolonToken) {

        return new STEnumDeclarationNode(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList,
                closeBraceToken,
                semicolonToken);
    }

    public static STNode createEnumMemberNode(
            STNode metadata,
            STNode identifier,
            STNode equalToken,
            STNode constExprNode) {

        return new STEnumMemberNode(
                metadata,
                identifier,
                equalToken,
                constExprNode);
    }

    public static STNode createArrayTypeDescriptorNode(
            STNode memberTypeDesc,
            STNode dimensions) {

        return new STArrayTypeDescriptorNode(
                memberTypeDesc,
                dimensions);
    }

    public static STNode createArrayDimensionNode(
            STNode openBracket,
            STNode arrayLength,
            STNode closeBracket) {

        return new STArrayDimensionNode(
                openBracket,
                arrayLength,
                closeBracket);
    }

    public static STNode createTransactionStatementNode(
            STNode transactionKeyword,
            STNode blockStatement,
            STNode onFailClause) {

        return new STTransactionStatementNode(
                transactionKeyword,
                blockStatement,
                onFailClause);
    }

    public static STNode createRollbackStatementNode(
            STNode rollbackKeyword,
            STNode expression,
            STNode semicolon) {

        return new STRollbackStatementNode(
                rollbackKeyword,
                expression,
                semicolon);
    }

    public static STNode createRetryStatementNode(
            STNode retryKeyword,
            STNode typeParameter,
            STNode arguments,
            STNode retryBody,
            STNode onFailClause) {

        return new STRetryStatementNode(
                retryKeyword,
                typeParameter,
                arguments,
                retryBody,
                onFailClause);
    }

    public static STNode createCommitActionNode(
            STNode commitKeyword) {

        return new STCommitActionNode(
                commitKeyword);
    }

    public static STNode createTransactionalExpressionNode(
            STNode transactionalKeyword) {

        return new STTransactionalExpressionNode(
                transactionalKeyword);
    }

    public static STNode createByteArrayLiteralNode(
            STNode type,
            STNode startBacktick,
            STNode content,
            STNode endBacktick) {

        return new STByteArrayLiteralNode(
                type,
                startBacktick,
                content,
                endBacktick);
    }

    public static STNode createXMLFilterExpressionNode(
            STNode expression,
            STNode xmlPatternChain) {

        return new STXMLFilterExpressionNode(
                expression,
                xmlPatternChain);
    }

    public static STNode createXMLStepExpressionNode(
            STNode expression,
            STNode xmlStepStart) {

        return new STXMLStepExpressionNode(
                expression,
                xmlStepStart);
    }

    public static STNode createXMLNamePatternChainingNode(
            STNode startToken,
            STNode xmlNamePattern,
            STNode gtToken) {

        return new STXMLNamePatternChainingNode(
                startToken,
                xmlNamePattern,
                gtToken);
    }

    public static STNode createXMLAtomicNamePatternNode(
            STNode prefix,
            STNode colon,
            STNode name) {

        return new STXMLAtomicNamePatternNode(
                prefix,
                colon,
                name);
    }

    public static STNode createTypeReferenceTypeDescNode(
            STNode typeRef) {

        return new STTypeReferenceTypeDescNode(
                typeRef);
    }

    public static STNode createMatchStatementNode(
            STNode matchKeyword,
            STNode condition,
            STNode openBrace,
            STNode matchClauses,
            STNode closeBrace,
            STNode onFailClause) {

        return new STMatchStatementNode(
                matchKeyword,
                condition,
                openBrace,
                matchClauses,
                closeBrace,
                onFailClause);
    }

    public static STNode createMatchClauseNode(
            STNode matchPatterns,
            STNode matchGuard,
            STNode rightDoubleArrow,
            STNode blockStatement) {

        return new STMatchClauseNode(
                matchPatterns,
                matchGuard,
                rightDoubleArrow,
                blockStatement);
    }

    public static STNode createMatchGuardNode(
            STNode ifKeyword,
            STNode expression) {

        return new STMatchGuardNode(
                ifKeyword,
                expression);
    }

    public static STNode createDistinctTypeDescriptorNode(
            STNode distinctKeyword,
            STNode typeDescriptor) {

        return new STDistinctTypeDescriptorNode(
                distinctKeyword,
                typeDescriptor);
    }

    public static STNode createListMatchPatternNode(
            STNode openBracket,
            STNode matchPatterns,
            STNode closeBracket) {

        return new STListMatchPatternNode(
                openBracket,
                matchPatterns,
                closeBracket);
    }

    public static STNode createRestMatchPatternNode(
            STNode ellipsisToken,
            STNode varKeywordToken,
            STNode variableName) {

        return new STRestMatchPatternNode(
                ellipsisToken,
                varKeywordToken,
                variableName);
    }

    public static STNode createMappingMatchPatternNode(
            STNode openBraceToken,
            STNode fieldMatchPatterns,
            STNode closeBraceToken) {

        return new STMappingMatchPatternNode(
                openBraceToken,
                fieldMatchPatterns,
                closeBraceToken);
    }

    public static STNode createFieldMatchPatternNode(
            STNode fieldNameNode,
            STNode colonToken,
            STNode matchPattern) {

        return new STFieldMatchPatternNode(
                fieldNameNode,
                colonToken,
                matchPattern);
    }

    public static STNode createErrorMatchPatternNode(
            STNode errorKeyword,
            STNode typeReference,
            STNode openParenthesisToken,
            STNode argListMatchPatternNode,
            STNode closeParenthesisToken) {

        return new STErrorMatchPatternNode(
                errorKeyword,
                typeReference,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken);
    }

    public static STNode createNamedArgMatchPatternNode(
            STNode identifier,
            STNode equalToken,
            STNode matchPattern) {

        return new STNamedArgMatchPatternNode(
                identifier,
                equalToken,
                matchPattern);
    }

    public static STNode createMarkdownDocumentationNode(
            STNode documentationLines) {

        return new STMarkdownDocumentationNode(
                documentationLines);
    }

    public static STNode createMarkdownDocumentationLineNode(
            SyntaxKind kind,
            STNode hashToken,
            STNode documentElements) {

        return new STMarkdownDocumentationLineNode(
                kind,
                hashToken,
                documentElements);
    }

    public static STNode createMarkdownParameterDocumentationLineNode(
            SyntaxKind kind,
            STNode hashToken,
            STNode plusToken,
            STNode parameterName,
            STNode minusToken,
            STNode documentElements) {

        return new STMarkdownParameterDocumentationLineNode(
                kind,
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements);
    }

    public static STNode createBallerinaNameReferenceNode(
            STNode referenceType,
            STNode startBacktick,
            STNode nameReference,
            STNode endBacktick) {

        return new STBallerinaNameReferenceNode(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick);
    }

    public static STNode createInlineCodeReferenceNode(
            STNode startBacktick,
            STNode codeReference,
            STNode endBacktick) {

        return new STInlineCodeReferenceNode(
                startBacktick,
                codeReference,
                endBacktick);
    }

    public static STNode createMarkdownCodeBlockNode(
            STNode startLineHashToken,
            STNode startBacktick,
            STNode langAttribute,
            STNode codeLines,
            STNode endLineHashToken,
            STNode endBacktick) {

        return new STMarkdownCodeBlockNode(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines,
                endLineHashToken,
                endBacktick);
    }

    public static STNode createMarkdownCodeLineNode(
            STNode hashToken,
            STNode codeDescription) {

        return new STMarkdownCodeLineNode(
                hashToken,
                codeDescription);
    }

    public static STNode createOrderByClauseNode(
            STNode orderKeyword,
            STNode byKeyword,
            STNode orderKey) {

        return new STOrderByClauseNode(
                orderKeyword,
                byKeyword,
                orderKey);
    }

    public static STNode createOrderKeyNode(
            STNode expression,
            STNode orderDirection) {

        return new STOrderKeyNode(
                expression,
                orderDirection);
    }

    public static STNode createOnFailClauseNode(
            STNode onKeyword,
            STNode failKeyword,
            STNode typeDescriptor,
            STNode failErrorName,
            STNode blockStatement) {

        return new STOnFailClauseNode(
                onKeyword,
                failKeyword,
                typeDescriptor,
                failErrorName,
                blockStatement);
    }

    public static STNode createDoStatementNode(
            STNode doKeyword,
            STNode blockStatement,
            STNode onFailClause) {

        return new STDoStatementNode(
                doKeyword,
                blockStatement,
                onFailClause);
    }

    public static STNode createClassDefinitionNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode classTypeQualifiers,
            STNode classKeyword,
            STNode className,
            STNode openBrace,
            STNode members,
            STNode closeBrace,
            STNode semicolonToken) {

        return new STClassDefinitionNode(
                metadata,
                visibilityQualifier,
                classTypeQualifiers,
                classKeyword,
                className,
                openBrace,
                members,
                closeBrace,
                semicolonToken);
    }

    public static STNode createResourcePathParameterNode(
            SyntaxKind kind,
            STNode openBracketToken,
            STNode annotations,
            STNode typeDescriptor,
            STNode ellipsisToken,
            STNode paramName,
            STNode closeBracketToken) {

        return new STResourcePathParameterNode(
                kind,
                openBracketToken,
                annotations,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken);
    }

    public static STNode createRequiredExpressionNode(
            STNode questionMarkToken) {

        return new STRequiredExpressionNode(
                questionMarkToken);
    }

    public static STNode createErrorConstructorExpressionNode(
            STNode errorKeyword,
            STNode typeReference,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {

        return new STErrorConstructorExpressionNode(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public static STNode createParameterizedTypeDescriptorNode(
            SyntaxKind kind,
            STNode keywordToken,
            STNode typeParamNode) {

        return new STParameterizedTypeDescriptorNode(
                kind,
                keywordToken,
                typeParamNode);
    }

    public static STNode createSpreadMemberNode(
            STNode ellipsis,
            STNode expression) {

        return new STSpreadMemberNode(
                ellipsis,
                expression);
    }

    public static STNode createClientResourceAccessActionNode(
            STNode expression,
            STNode rightArrowToken,
            STNode slashToken,
            STNode resourceAccessPath,
            STNode dotToken,
            STNode methodName,
            STNode arguments) {

        return new STClientResourceAccessActionNode(
                expression,
                rightArrowToken,
                slashToken,
                resourceAccessPath,
                dotToken,
                methodName,
                arguments);
    }

    public static STNode createComputedResourceAccessSegmentNode(
            STNode openBracketToken,
            STNode expression,
            STNode closeBracketToken) {

        return new STComputedResourceAccessSegmentNode(
                openBracketToken,
                expression,
                closeBracketToken);
    }

    public static STNode createResourceAccessRestSegmentNode(
            STNode openBracketToken,
            STNode ellipsisToken,
            STNode expression,
            STNode closeBracketToken) {

        return new STResourceAccessRestSegmentNode(
                openBracketToken,
                ellipsisToken,
                expression,
                closeBracketToken);
    }

    public static STNode createReSequenceNode(
            STNode reTerm) {

        return new STReSequenceNode(
                reTerm);
    }

    public static STNode createReAtomQuantifierNode(
            STNode reAtom,
            STNode reQuantifier) {

        return new STReAtomQuantifierNode(
                reAtom,
                reQuantifier);
    }

    public static STNode createReAtomCharOrEscapeNode(
            STNode reAtomCharOrEscape) {

        return new STReAtomCharOrEscapeNode(
                reAtomCharOrEscape);
    }

    public static STNode createReQuoteEscapeNode(
            STNode slashToken,
            STNode reSyntaxChar) {

        return new STReQuoteEscapeNode(
                slashToken,
                reSyntaxChar);
    }

    public static STNode createReSimpleCharClassEscapeNode(
            STNode slashToken,
            STNode reSimpleCharClassCode) {

        return new STReSimpleCharClassEscapeNode(
                slashToken,
                reSimpleCharClassCode);
    }

    public static STNode createReUnicodePropertyEscapeNode(
            STNode slashToken,
            STNode property,
            STNode openBraceToken,
            STNode reUnicodeProperty,
            STNode closeBraceToken) {

        return new STReUnicodePropertyEscapeNode(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken);
    }

    public static STNode createReUnicodeScriptNode(
            STNode scriptStart,
            STNode reUnicodePropertyValue) {

        return new STReUnicodeScriptNode(
                scriptStart,
                reUnicodePropertyValue);
    }

    public static STNode createReUnicodeGeneralCategoryNode(
            STNode categoryStart,
            STNode reUnicodeGeneralCategoryName) {

        return new STReUnicodeGeneralCategoryNode(
                categoryStart,
                reUnicodeGeneralCategoryName);
    }

    public static STNode createReCharacterClassNode(
            STNode openBracket,
            STNode negation,
            STNode reCharSet,
            STNode closeBracket) {

        return new STReCharacterClassNode(
                openBracket,
                negation,
                reCharSet,
                closeBracket);
    }

    public static STNode createReCharSetRangeWithReCharSetNode(
            STNode reCharSetRange,
            STNode reCharSet) {

        return new STReCharSetRangeWithReCharSetNode(
                reCharSetRange,
                reCharSet);
    }

    public static STNode createReCharSetRangeNode(
            STNode lhsReCharSetAtom,
            STNode minusToken,
            STNode rhsReCharSetAtom) {

        return new STReCharSetRangeNode(
                lhsReCharSetAtom,
                minusToken,
                rhsReCharSetAtom);
    }

    public static STNode createReCharSetAtomWithReCharSetNoDashNode(
            STNode reCharSetAtom,
            STNode reCharSetNoDash) {

        return new STReCharSetAtomWithReCharSetNoDashNode(
                reCharSetAtom,
                reCharSetNoDash);
    }

    public static STNode createReCharSetRangeNoDashWithReCharSetNode(
            STNode reCharSetRangeNoDash,
            STNode reCharSet) {

        return new STReCharSetRangeNoDashWithReCharSetNode(
                reCharSetRangeNoDash,
                reCharSet);
    }

    public static STNode createReCharSetRangeNoDashNode(
            STNode reCharSetAtomNoDash,
            STNode minusToken,
            STNode reCharSetAtom) {

        return new STReCharSetRangeNoDashNode(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom);
    }

    public static STNode createReCharSetAtomNoDashWithReCharSetNoDashNode(
            STNode reCharSetAtomNoDash,
            STNode reCharSetNoDash) {

        return new STReCharSetAtomNoDashWithReCharSetNoDashNode(
                reCharSetAtomNoDash,
                reCharSetNoDash);
    }

    public static STNode createReCapturingGroupsNode(
            STNode openParenthesis,
            STNode reFlagExpression,
            STNode reSequences,
            STNode closeParenthesis) {

        return new STReCapturingGroupsNode(
                openParenthesis,
                reFlagExpression,
                reSequences,
                closeParenthesis);
    }

    public static STNode createReFlagExpressionNode(
            STNode questionMark,
            STNode reFlagsOnOff,
            STNode colon) {

        return new STReFlagExpressionNode(
                questionMark,
                reFlagsOnOff,
                colon);
    }

    public static STNode createReFlagsOnOffNode(
            STNode lhsReFlags,
            STNode minusToken,
            STNode rhsReFlags) {

        return new STReFlagsOnOffNode(
                lhsReFlags,
                minusToken,
                rhsReFlags);
    }

    public static STNode createReFlagsNode(
            STNode reFlag) {

        return new STReFlagsNode(
                reFlag);
    }

    public static STNode createReAssertionNode(
            STNode reAssertion) {

        return new STReAssertionNode(
                reAssertion);
    }

    public static STNode createReQuantifierNode(
            STNode reBaseQuantifier,
            STNode nonGreedyChar) {

        return new STReQuantifierNode(
                reBaseQuantifier,
                nonGreedyChar);
    }

    public static STNode createReBracedQuantifierNode(
            STNode openBraceToken,
            STNode leastTimesMatchedDigit,
            STNode commaToken,
            STNode mostTimesMatchedDigit,
            STNode closeBraceToken) {

        return new STReBracedQuantifierNode(
                openBraceToken,
                leastTimesMatchedDigit,
                commaToken,
                mostTimesMatchedDigit,
                closeBraceToken);
    }
}

