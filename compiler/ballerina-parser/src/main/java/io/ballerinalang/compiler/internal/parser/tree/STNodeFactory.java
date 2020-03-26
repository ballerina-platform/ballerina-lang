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

import java.util.List;

/**
 * A factory that constructs internal tree nodes.
 * <p>
 * This class contains various helper methods that create internal tree nodes.
 * <p>
 * Note that {@code STNodeFactory} must be used to create {@code STNode} instances. This approach allows
 * us to manage {@code STNode} production in the future. We could load nodes from a cache or add debug logs etc.
 *
 * @since 1.3.0
 */
public class STNodeFactory {

    private STNodeFactory() {
    }

    public static STNode makeModulePart(STNode imports, STNode members, STNode eofToken) {
        return new STModulePart(imports, members, eofToken);
    }

    // Module level declarations

    public static STNode makeFunctionDefinition(STNode visibilityQualifier,
                                         STNode functionKeyword,
                                         STNode functionName,
                                         STNode openParenToken,
                                         STNode parameters,
                                         STNode closeParenToken,
                                         STNode returnTypeDesc,
                                         STNode functionBody) {

        return new STFunctionDefinition(visibilityQualifier, functionKeyword, functionName,
                openParenToken, parameters, closeParenToken, returnTypeDesc, functionBody);
    }

    public static STNode makeModuleTypeDefinition(STNode visibilityQualifier,
                                                  STNode typeKeyword,
                                                  STNode typeName,
                                                  STNode typeDescriptor,
                                                  STNode comma) {
        return new STModuleTypeDefinition(visibilityQualifier, typeKeyword, typeName, typeDescriptor, comma);
    }

    // Statements

    public static STNode makeAssignmentStatement(SyntaxKind kind,
                                                 STNode lhsExpression,
                                                 STNode equalsToken,
                                                 STNode expr,
                                                 STNode semicolonToken) {
        return new STAssignmentStatement(kind, lhsExpression, equalsToken, expr, semicolonToken);
    }

    public static STNode makeWhileStatement(STNode whileKeyword, STNode condition, STNode whileBody) {
        return new STWhileStatement(whileKeyword, condition, whileBody);
    }

    public static STNode makeBlockStatement(STNode openBraceToken, STNode statements, STNode closeBraceToken) {
        return new STBlockStatement(openBraceToken, statements, closeBraceToken);
    }

    public static STNode makeCallStatement(STNode expression, STNode semicolonToken) {
        return new STCallStatement(expression, semicolonToken);
    }

    public static STNode makeElseBlock(STNode elseKeyword, STNode elseBody) {
        return new STElseBlock(elseKeyword, elseBody);
    }

    public static STNode makeExternalFunctionBody(SyntaxKind kind,
                                                  STNode assign,
                                                  STNode annotation,
                                                  STNode externalKeyword,
                                                  STNode semicolon) {
        return new STExternalFuncBody(kind, assign, annotation, externalKeyword, semicolon);
    }

    public static STNode makeIfElseStatement(STNode ifKeyword,
                                             STNode condition,
                                             STNode ifBody,
                                             STNode elseBody) {
        return new STIfElseStatement(ifKeyword, condition, ifBody, elseBody);
    }

    public static STNode makeVariableDeclaration(SyntaxKind kind,
                                                 STNode typeName,
                                                 STNode variableName,
                                                 STNode equalsToken,
                                                 STNode initializer,
                                                 STNode semicolonToken) {
        return new STVariableDeclaration(kind, typeName, variableName, equalsToken, initializer, semicolonToken);
    }

    // Expressions

    public static STNode makeBinaryExpression(SyntaxKind kind,
                                              STNode lhsExpr,
                                              STNode operator,
                                              STNode rhsExpr) {
        return new STBinaryExpression(kind, lhsExpr, operator, rhsExpr);
    }

    public static STNode makeBracedExpression(SyntaxKind kind,
                                              STNode openParen,
                                              STNode expr,
                                              STNode closeParen) {
        return new STBracedExpression(kind, openParen, expr, closeParen);
    }

    public static STNode makeCheckExpression(STNode checkingKeyword, STNode rhsExpr) {
        return new STCheckExpression(checkingKeyword, rhsExpr);
    }

    public static STNode makeFieldAccessExpression(STNode expression, STNode dotToken, STNode fieldName) {
        return new STFieldAccessExpression(expression, dotToken, fieldName);
    }

    public static STNode makeFunctionCallExpression(STNode functionName,
                                                    STNode openParen,
                                                    STNode arguments,
                                                    STNode closeParen) {
        return new STFunctionCallExpression(functionName, openParen, arguments, closeParen);
    }

    public static STNode makeMemberAccessExpression(STNode containerExpr,
                                                    STNode openBracket,
                                                    STNode keyExpression,
                                                    STNode closeBracket) {
        return new STMemberAccessExpression(containerExpr, openBracket, keyExpression, closeBracket);
    }

    public static STNode makeMethodCallExpression(STNode expression,
                                                  STNode dotToken,
                                                  STNode methodName,
                                                  STNode openParen,
                                                  STNode arguments,
                                                  STNode closeParen) {
        return new STMethodCallExpression(expression, dotToken, methodName, openParen, arguments, closeParen);
    }

    // Misc

    public static STNode makeRequiredParameter(SyntaxKind kind,
                                               STNode leadingComma,
                                               STNode accessModifier,
                                               STNode type,
                                               STNode paramName) {
        return new STRequiredParameter(kind, leadingComma, accessModifier, type, paramName);
    }

    public static STNode makeDefaultableParameter(SyntaxKind kind,
                                                  STNode leadingComma,
                                                  STNode visibilityQualifier,
                                                  STNode type,
                                                  STNode paramName,
                                                  STNode equal,
                                                  STNode expr) {
        return new STDefaultableParameter(kind, leadingComma, visibilityQualifier, type, paramName, equal, expr);
    }

    public static STNode makeRestParameter(SyntaxKind kind,
                                           STNode leadingComma,
                                           STNode type,
                                           STNode ellipsis,
                                           STNode paramName) {
        return new STRestParameter(kind, leadingComma, type, ellipsis, paramName);
    }

    public static STNode makePositionalArg(STNode leadingComma, STNode expression) {
        return new STPositionalArg(leadingComma, expression);
    }

    public static STNode makeNamedArg(STNode leadingComma,
                                      STNode variableName,
                                      STNode equalsToken,
                                      STNode expression) {
        return new STNamedArg(leadingComma, variableName, equalsToken, expression);
    }

    public static STNode makeRestArg(STNode leadingComma,
                                     STNode ellipsis,
                                     STNode expression) {
        return new STRestArg(leadingComma, ellipsis, expression);
    }

    public static STNode makeObjectField(STNode visibilityQualifier,
                                         STNode type,
                                         STNode fieldName,
                                         STNode equalsToken,
                                         STNode expression,
                                         STNode semicolonToken) {
        return new STObjectField(visibilityQualifier, type, fieldName, equalsToken, expression, semicolonToken);
    }

    public static STNode makeRecordField(STNode type,
                                         STNode fieldName,
                                         STNode questionMarkToken,
                                         STNode semicolonToken) {
        return new STRecordField(type, fieldName, questionMarkToken, semicolonToken);
    }

    public static STNode makeRecordFieldWithDefaultValue(STNode type,
                                                         STNode fieldName,
                                                         STNode equalsToken,
                                                         STNode expression,
                                                         STNode semicolonToken) {
        return new STRecordFieldWithDefaultValue(type, fieldName, equalsToken, expression, semicolonToken);
    }

    public static STNode makeRecordRestDescriptor(STNode type, STNode ellipsis, STNode semicolonToken) {
        return new STRecordRestDescriptor(type, ellipsis, semicolonToken);
    }

    public static STNode makeObjectTypeDescriptor(STNode objectTypeQualifiers,
                                                  STNode objectKeyword,
                                                  STNode openBrace,
                                                  STNode members,
                                                  STNode closeBrace) {
        return new STObjectTypeDescriptor(objectTypeQualifiers, objectKeyword, openBrace, members, closeBrace);
    }

    public static STNode makeRecordTypeDescriptor(STNode recordKeyword,
                                                  STNode bodyStartDelimiter,
                                                  STNode fields,
                                                  STNode bodyEndDelimiter) {
        return new STRecordTypeDescriptor(recordKeyword, bodyStartDelimiter, fields, bodyEndDelimiter);
    }

    public static STNode makeReturnTypeDescriptor(STNode returnsKeyword,
                                                  STNode annotation,
                                                  STNode type) {
        return new STReturnTypeDescriptor(returnsKeyword, annotation, type);
    }

    public static STNode makeTypeReference(STNode asterisk,
                                           STNode type,
                                           STNode semicolonToken) {
        return new STTypeReference(asterisk, type, semicolonToken);
    }

    public static STNode makeQualifiedIdentifier(STNode modulePrefix, STNode colon, STNode identifier) {
        return new STQualifiedIdentifier(modulePrefix, colon, identifier);
    }

    public static STToken makeIdentifier(String text, STNode leadingTrivia, STNode trailingTrivia) {
        return new STIdentifier(text, leadingTrivia, trailingTrivia);
    }

    public static STNode makeNodeList(List<STNode> children) {
        return new STNodeList(children);
    }

    public static STNode makeEmptyNode() {
        // TODO Seems like we can use a single instance of this node
        return new STEmptyNode();
    }

    public static STNode makeMissingToken(SyntaxKind kind) {
        // TODO Seems like we can get these tokens from a cache
        return new STMissingToken(kind);
    }

    public static STToken makeToken(SyntaxKind kind, STNode leadingTrivia, STNode trailingTrivia) {
        return new STToken(kind, leadingTrivia, trailingTrivia);
    }

    public static STToken makeToken(SyntaxKind kind, int width, STNode leadingTrivia, STNode trailingTrivia) {
        return new STToken(kind, width, leadingTrivia, trailingTrivia);
    }

    public static STToken makeTypeToken(SyntaxKind kind, String text, STNode leadingTrivia, STNode trailingTrivia) {
        return new STTypeToken(kind, text, leadingTrivia, trailingTrivia);
    }

    public static STToken makeLiteralValueToken(SyntaxKind kind,
                                                String text,
                                                long value,
                                                STNode leadingTrivia,
                                                STNode trailingTrivia) {
        return new STLiteralValueToken(kind, text, value, leadingTrivia, trailingTrivia
        );
    }
}
