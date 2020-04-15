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

    public static STNode createModulePart(
            STNode imports,
            STNode members,
            STNode eofToken) {

        return new STModulePart(
                imports,
                members,
                eofToken);
    }

    public static STNode createFunctionDefinition(
            STNode metadata,
            STNode visibilityQualifier,
            STNode functionKeyword,
            STNode functionName,
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken,
            STNode returnTypeDesc,
            STNode functionBody) {

        return new STFunctionDefinition(
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

    public static STNode createImportDeclaration(
            STNode importKeyword,
            STNode orgName,
            STNode moduleName,
            STNode version,
            STNode prefix,
            STNode semicolon) {

        return new STImportDeclaration(
                importKeyword,
                orgName,
                moduleName,
                version,
                prefix,
                semicolon);
    }

    public static STNode createListenerDeclaration(
            STNode metadata,
            STNode visibilityQualifier,
            STNode listenerKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STListenerDeclaration(
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

    public static STNode createServiceDeclaration(
            STNode metadata,
            STNode serviceKeyword,
            STNode serviceName,
            STNode onKeyword,
            STNode expressions,
            STNode serviceBody) {

        return new STServiceDeclaration(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody);
    }

    public static STNode createAssignmentStatement(
            STNode varRef,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {

        return new STAssignmentStatement(
                varRef,
                equalsToken,
                expression,
                semicolonToken);
    }

    public static STNode createCompoundAssignmentStatement(
            STNode lhsExpression,
            STNode binaryOperator,
            STNode equalsToken,
            STNode rhsExpression,
            STNode semicolonToken) {

        return new STCompoundAssignmentStatement(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken);
    }

    public static STNode createVariableDeclaration(
            STNode annotations,
            STNode finalKeyword,
            STNode typeName,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STVariableDeclaration(
                annotations,
                finalKeyword,
                typeName,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public static STNode createBlockStatement(
            STNode openBraceToken,
            STNode statements,
            STNode closeBraceToken) {

        return new STBlockStatement(
                openBraceToken,
                statements,
                closeBraceToken);
    }

    public static STNode createBreakStatement(
            STNode breakToken,
            STNode semicolonToken) {

        return new STBreakStatement(
                breakToken,
                semicolonToken);
    }

    public static STNode createCallStatement(
            STNode expression,
            STNode semicolonToken) {

        return new STCallStatement(
                expression,
                semicolonToken);
    }

    public static STNode createContinueStatement(
            STNode continueToken,
            STNode semicolonToken) {

        return new STContinueStatement(
                continueToken,
                semicolonToken);
    }

    public static STNode createExternalFunctionBody(
            STNode equalsToken,
            STNode annotations,
            STNode externalKeyword,
            STNode semicolonToken) {

        return new STExternalFunctionBody(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken);
    }

    public static STNode createIfElseStatement(
            STNode ifKeyword,
            STNode condition,
            STNode ifBody,
            STNode elseBody) {

        return new STIfElseStatement(
                ifKeyword,
                condition,
                ifBody,
                elseBody);
    }

    public static STNode createElseBlock(
            STNode elseKeyword,
            STNode elseBody) {

        return new STElseBlock(
                elseKeyword,
                elseBody);
    }

    public static STNode createWhileStatement(
            STNode whileKeyword,
            STNode condition,
            STNode whileBody) {

        return new STWhileStatement(
                whileKeyword,
                condition,
                whileBody);
    }

    public static STNode createPanicStatement(
            STNode panicKeyword,
            STNode expression,
            STNode semicolonToken) {

        return new STPanicStatement(
                panicKeyword,
                expression,
                semicolonToken);
    }

    public static STNode createReturnStatement(
            STNode returnKeyword,
            STNode expression,
            STNode semicolonToken) {

        return new STReturnStatement(
                returnKeyword,
                expression,
                semicolonToken);
    }

    public static STNode createBinaryExpression(
            SyntaxKind kind,
            STNode lhsExpr,
            STNode operator,
            STNode rhsExpr) {

        return new STBinaryExpression(
                kind,
                lhsExpr,
                operator,
                rhsExpr);
    }

    public static STNode createBracedExpression(
            SyntaxKind kind,
            STNode openParen,
            STNode expression,
            STNode closeParen) {

        return new STBracedExpression(
                kind,
                openParen,
                expression,
                closeParen);
    }

    public static STNode createCheckExpression(
            STNode checkKeyword,
            STNode expression) {

        return new STCheckExpression(
                checkKeyword,
                expression);
    }

    public static STNode createFieldAccessExpression(
            STNode expression,
            STNode dotToken,
            STNode fieldName) {

        return new STFieldAccessExpression(
                expression,
                dotToken,
                fieldName);
    }

    public static STNode createFunctionCallExpression(
            STNode functionName,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {

        return new STFunctionCallExpression(
                functionName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public static STNode createMethodCallExpression(
            STNode expression,
            STNode dotToken,
            STNode methodName,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {

        return new STMethodCallExpression(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public static STNode createMappingConstructorExpression(
            STNode openBrace,
            STNode fields,
            STNode closeBrace) {

        return new STMappingConstructorExpression(
                openBrace,
                fields,
                closeBrace);
    }

    public static STNode createMemberAccessExpression(
            STNode containerExpression,
            STNode openBracket,
            STNode keyExpression,
            STNode closeBracket) {

        return new STMemberAccessExpression(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket);
    }

    public static STNode createTypeofExpression(
            STNode typeofKeyword,
            STNode expression) {

        return new STTypeofExpression(
                typeofKeyword,
                expression);
    }

    public static STNode createUnaryExpression(
            STNode unaryOperator,
            STNode expression) {

        return new STUnaryExpression(
                unaryOperator,
                expression);
    }

    public static STNode createComputedNameField(
            STNode leadingComma,
            STNode openBracket,
            STNode fieldNameExpr,
            STNode closeBracket,
            STNode colonToken,
            STNode valueExpr) {

        return new STComputedNameField(
                leadingComma,
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr);
    }

    public static STNode createConstantDeclaration(
            STNode metadata,
            STNode visibilityQualifier,
            STNode constKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STConstantDeclaration(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public static STNode createDefaultableParameter(
            STNode leadingComma,
            STNode annotations,
            STNode visibilityQualifier,
            STNode type,
            STNode paramName,
            STNode equalsToken,
            STNode expression) {

        return new STDefaultableParameter(
                leadingComma,
                annotations,
                visibilityQualifier,
                type,
                paramName,
                equalsToken,
                expression);
    }

    public static STNode createRequiredParameter(
            STNode leadingComma,
            STNode annotations,
            STNode visibilityQualifier,
            STNode type,
            STNode paramName) {

        return new STRequiredParameter(
                leadingComma,
                annotations,
                visibilityQualifier,
                type,
                paramName);
    }

    public static STNode createRestParameter(
            STNode leadingComma,
            STNode annotations,
            STNode type,
            STNode ellipsisToken,
            STNode paramName) {

        return new STRestParameter(
                leadingComma,
                annotations,
                type,
                ellipsisToken,
                paramName);
    }

    public static STNode createExpressionListItem(
            STNode leadingComma,
            STNode expression) {

        return new STExpressionListItem(
                leadingComma,
                expression);
    }

    public static STNode createImportOrgName(
            STNode orgName,
            STNode slashToken) {

        return new STImportOrgName(
                orgName,
                slashToken);
    }

    public static STNode createImportPrefix(
            STNode asKeyword,
            STNode prefix) {

        return new STImportPrefix(
                asKeyword,
                prefix);
    }

    public static STNode createImportSubVersion(
            STNode leadingDot,
            STNode versionNumber) {

        return new STImportSubVersion(
                leadingDot,
                versionNumber);
    }

    public static STNode createImportVersion(
            STNode versionKeyword,
            STNode versionNumber) {

        return new STImportVersion(
                versionKeyword,
                versionNumber);
    }

    public static STNode createSubModuleName(
            STNode leadingDot,
            STNode moduleName) {

        return new STSubModuleName(
                leadingDot,
                moduleName);
    }

    public static STNode createSpecificField(
            STNode leadingComma,
            STNode fieldName,
            STNode colon,
            STNode valueExpr) {

        return new STSpecificField(
                leadingComma,
                fieldName,
                colon,
                valueExpr);
    }

    public static STNode createSpreadField(
            STNode leadingComma,
            STNode ellipsis,
            STNode valueExpr) {

        return new STSpreadField(
                leadingComma,
                ellipsis,
                valueExpr);
    }

    public static STNode createNamedArgument(
            STNode leadingComma,
            STNode argumentName,
            STNode equalsToken,
            STNode expression) {

        return new STNamedArgument(
                leadingComma,
                argumentName,
                equalsToken,
                expression);
    }

    public static STNode createPositionalArgument(
            STNode leadingComma,
            STNode expression) {

        return new STPositionalArgument(
                leadingComma,
                expression);
    }

    public static STNode createRestArgument(
            STNode leadingComma,
            STNode ellipsis,
            STNode expression) {

        return new STRestArgument(
                leadingComma,
                ellipsis,
                expression);
    }

    public static STNode createObjectTypeDescriptor(
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode openBrace,
            STNode members,
            STNode closeBrace) {

        return new STObjectTypeDescriptor(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    public static STNode createRecordTypeDescriptor(
            STNode objectKeyword,
            STNode bodyStartDelimiter,
            STNode fields,
            STNode bodyEndDelimiter) {

        return new STRecordTypeDescriptor(
                objectKeyword,
                bodyStartDelimiter,
                fields,
                bodyEndDelimiter);
    }

    public static STNode createReturnTypeDescriptor(
            STNode returnsKeyword,
            STNode annotations,
            STNode type) {

        return new STReturnTypeDescriptor(
                returnsKeyword,
                annotations,
                type);
    }

    public static STNode createNilTypeDescriptor(
            STNode openParenToken,
            STNode closeParenToken) {

        return new STNilTypeDescriptor(
                openParenToken,
                closeParenToken);
    }

    public static STNode createOptionalTypeDescriptor(
            STNode typeDescriptor,
            STNode questionMarkToken) {

        return new STOptionalTypeDescriptor(
                typeDescriptor,
                questionMarkToken);
    }

    public static STNode createObjectField(
            STNode metadata,
            STNode visibilityQualifier,
            STNode type,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {

        return new STObjectField(
                metadata,
                visibilityQualifier,
                type,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    public static STNode createRecordField(
            STNode metadata,
            STNode type,
            STNode fieldName,
            STNode questionMarkToken,
            STNode semicolonToken) {

        return new STRecordField(
                metadata,
                type,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    public static STNode createRecordFieldWithDefaultValue(
            STNode metadata,
            STNode type,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {

        return new STRecordFieldWithDefaultValue(
                metadata,
                type,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    public static STNode createRecordRestDescriptor(
            STNode type,
            STNode ellipsisToken,
            STNode semicolonToken) {

        return new STRecordRestDescriptor(
                type,
                ellipsisToken,
                semicolonToken);
    }

    public static STNode createTypeReference(
            STNode asteriskToken,
            STNode type,
            STNode semicolonToken) {

        return new STTypeReference(
                asteriskToken,
                type,
                semicolonToken);
    }

    public static STNode createQualifiedIdentifier(
            STNode modulePrefix,
            STNode colon,
            STNode identifier) {

        return new STQualifiedIdentifier(
                modulePrefix,
                colon,
                identifier);
    }

    public static STNode createServiceBody(
            STNode openBraceToken,
            STNode resources,
            STNode closeBraceToken) {

        return new STServiceBody(
                openBraceToken,
                resources,
                closeBraceToken);
    }

    public static STNode createAnnotation(
            STNode atToken,
            STNode annotReference,
            STNode annotValue) {

        return new STAnnotation(
                atToken,
                annotReference,
                annotValue);
    }

    public static STNode createMetadata(
            STNode documentationString,
            STNode annotations) {

        return new STMetadata(
                documentationString,
                annotations);
    }

    public static STNode createModuleVariableDeclaration(
            STNode metadata,
            STNode finalKeyword,
            STNode typeName,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {

        return new STModuleVariableDeclaration(
                metadata,
                finalKeyword,
                typeName,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public static STNode createIsExpression(
            STNode expression,
            STNode isKeyword,
            STNode typeDescriptor) {

        return new STIsExpression(
                expression,
                isKeyword,
                typeDescriptor);
    }

    public static STNode createNilLiteral(
            STNode firstToken,
            STNode secondToken) {

        return new STNilLiteral(
                firstToken,
                secondToken);
    }
}

