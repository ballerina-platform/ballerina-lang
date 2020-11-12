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
package io.ballerina.toml.internal.parser;

import io.ballerina.toml.internal.diagnostics.DiagnosticCode;
import io.ballerina.toml.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.toml.internal.parser.tree.STNodeFactory;
import io.ballerina.toml.internal.parser.tree.STNodeList;
import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.toml.internal.syntax.NodeListUtils;
import io.ballerina.toml.internal.syntax.SyntaxUtils;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class contains utilities to handle syntax errors.
 *
 * @since 2.0.0
 */
public class SyntaxErrors {

    private SyntaxErrors() {
    }

    public static STNodeDiagnostic createDiagnostic(DiagnosticCode diagnosticCode, Object... args) {
        return STNodeDiagnostic.from(diagnosticCode, args);
    }

    public static <T extends STNode> T addDiagnostic(T node, DiagnosticCode diagnosticCode, Object... args) {
        return addSyntaxDiagnostic(node, createDiagnostic(diagnosticCode, args));
    }

    public static <T extends STNode> T addSyntaxDiagnostic(T node, STNodeDiagnostic diagnostic) {
        return addSyntaxDiagnostics(node, Collections.singletonList(diagnostic));
    }

    @SuppressWarnings("unchecked")
    public static <T extends STNode> T addSyntaxDiagnostics(T node, Collection<STNodeDiagnostic> diagnostics) {
        if (diagnostics.isEmpty()) {
            return node;
        }

        Collection<STNodeDiagnostic> newDiagnostics;
        Collection<STNodeDiagnostic> oldDiagnostics = node.diagnostics();
        if (oldDiagnostics.isEmpty()) {
            newDiagnostics = new ArrayList<>(diagnostics);
        } else {
            // Merge all diagnostics
            newDiagnostics = new ArrayList<>(oldDiagnostics);
            newDiagnostics.addAll(diagnostics);
        }
        return (T) node.modifyWith(newDiagnostics);
    }

    public static STToken createMissingToken(SyntaxKind expectedKind) {
        return STNodeFactory.createMissingToken(expectedKind);
    }
    // TODO check for possibility of removing this method
    public static STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind) {
        return createMissingTokenWithDiagnostics(expectedKind, getErrorCode(expectedKind));
    }

    public static STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind, ParserRuleContext currentCtx) {
        return createMissingTokenWithDiagnostics(expectedKind, getErrorCode(currentCtx));
    }

    public static STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind,
                                                            DiagnosticCode diagnosticCode) {
        List<STNodeDiagnostic> diagnosticList = new ArrayList<>();
        diagnosticList.add(createDiagnostic(diagnosticCode));
        return STNodeFactory.createMissingToken(expectedKind, diagnosticList);
    }

    // TODO check for possibility of removing this method
    private static DiagnosticCode getErrorCode(SyntaxKind expectedKind) {
        switch (expectedKind) {

            // Separators
            case OPEN_BRACKET_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACKET_TOKEN;
            case CLOSE_BRACKET_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACKET_TOKEN;
            case DOT_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_DOT_TOKEN;
            case COMMA_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_COMMA_TOKEN;
            case HASH_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_HASH_TOKEN;
            case DOUBLE_QUOTE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_DOUBLE_QUOTE_TOKEN;
            case SINGLE_QUOTE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_SINGLE_QUOTE_TOKEN;

            // Operators
            case EQUAL_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_EQUAL_TOKEN;
            case PLUS_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_PLUS_TOKEN;
            case MINUS_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_MINUS_TOKEN;

            case IDENTIFIER_LITERAL:
                return DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER;
            case STRING_LITERAL:
                return DiagnosticErrorCode.ERROR_MISSING_STRING_LITERAL;
            default:
                return DiagnosticErrorCode.ERROR_SYNTAX_ERROR;
        }
    }

    private static DiagnosticCode getErrorCode(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case KEY:
                return DiagnosticErrorCode.ERROR_MISSING_KEY;
            case VALUE:
                return DiagnosticErrorCode.ERROR_MISSING_VALUE;
            case BASIC_LITERAL: // return var-ref for any kind of terminal expression
                return DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER;
            case STRING_LITERAL:
                return DiagnosticErrorCode.ERROR_MISSING_STRING_LITERAL;
            default:
                return getSeperatorTokenErrorCode(currentCtx);
        }
    }

    private static DiagnosticCode getSeperatorTokenErrorCode(ParserRuleContext ctx) {
        switch (ctx) {
            case ASSIGN_OP:
                return DiagnosticErrorCode.ERROR_MISSING_EQUAL_TOKEN;
            case PLUS_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_PLUS_TOKEN;
            case CLOSE_BRACKET:
            case ARRAY_VALUE_LIST_END:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACKET_TOKEN;
            case COMMA:
                return DiagnosticErrorCode.ERROR_MISSING_COMMA_TOKEN;
            case OPEN_BRACKET:
            case ARRAY_VALUE_LIST_START:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACKET_TOKEN;
            case DOUBLE_CLOSE_BRACKET:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_DOUBLE_BRACKET;
            case DOT:
                return DiagnosticErrorCode.ERROR_MISSING_DOT_TOKEN;
            case DOUBLE_OPEN_BRACKET:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_DOUBLE_BRACKET;
            default:
                return DiagnosticErrorCode.ERROR_SYNTAX_ERROR;
        }
    }

    /**
     * Clone the given {@code STNode} with the invalid node as leading minutiae.
     *
     * @param toClone     the node to be cloned
     * @param invalidNode the invalid node
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithLeadingInvalidNodeMinutiae(STNode toClone, STNode invalidNode) {
        return cloneWithLeadingInvalidNodeMinutiae(toClone, invalidNode, null);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as leading minutiae.
     *
     * @param toClone        the node to be cloned
     * @param invalidNode    the invalid node
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithLeadingInvalidNodeMinutiae(STNode toClone,
                                                             STNode invalidNode,
                                                             DiagnosticCode diagnosticCode,
                                                             Object... args) {
        STToken firstToken = toClone.firstToken();
        STToken firstTokenWithInvalidNodeMinutiae = cloneWithLeadingInvalidNodeMinutiae(firstToken,
                invalidNode, diagnosticCode, args);
        return toClone.replace(firstToken, firstTokenWithInvalidNodeMinutiae);
    }

    /**
     * Clone the given {@code STToken} with the invalid node as leading minutiae.
     *
     * @param toClone        the token to be cloned
     * @param invalidNode    the invalid node
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a cloned token with the given invalidNode minutiae
     */
    public static STToken cloneWithLeadingInvalidNodeMinutiae(STToken toClone,
                                                              STNode invalidNode,
                                                              DiagnosticCode diagnosticCode,
                                                              Object... args) {
        List<STNode> minutiaeList = convertInvalidNodeToMinutiae(invalidNode);
        STNodeList leadingMinutiae = (STNodeList) toClone.leadingMinutiae();
        leadingMinutiae = leadingMinutiae.addAll(0, minutiaeList);
        STToken cloned = toClone.modifyWith(leadingMinutiae, toClone.trailingMinutiae());
        return diagnosticCode == null ? cloned : addDiagnostic(cloned, diagnosticCode, args);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as trailing minutiae.
     *
     * @param toClone     the node to be cloned
     * @param invalidNode the invalid node
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithTrailingInvalidNodeMinutiae(STNode toClone, STNode invalidNode) {
        return cloneWithTrailingInvalidNodeMinutiae(toClone, invalidNode, null);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as trailing minutiae.
     *
     * @param toClone        the node to be cloned
     * @param invalidNode    the invalid node
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithTrailingInvalidNodeMinutiae(STNode toClone,
                                                              STNode invalidNode,
                                                              DiagnosticCode diagnosticCode,
                                                              Object... args) {
        STToken lastToken = toClone.lastToken();
        STToken lastTokenWithInvalidNodeMinutiae = cloneWithTrailingInvalidNodeMinutiae(lastToken,
                invalidNode, diagnosticCode, args);
        return toClone.replace(lastToken, lastTokenWithInvalidNodeMinutiae);
    }

    /**
     * Clone the given {@code STToken} with the invalid node as trailing minutiae.
     *
     * @param toClone        the token to be cloned
     * @param invalidNode    the invalid node
     * @param diagnosticCode the {@code DiagnosticCode} to be added
     * @param args           additional arguments required to format the diagnostic message
     * @return a cloned token with the given invalidNode minutiae
     */
    public static STToken cloneWithTrailingInvalidNodeMinutiae(STToken toClone,
                                                               STNode invalidNode,
                                                               DiagnosticCode diagnosticCode,
                                                               Object... args) {
        List<STNode> minutiaeList = convertInvalidNodeToMinutiae(invalidNode);
        STNodeList trailingMinutiae = (STNodeList) toClone.trailingMinutiae();
        trailingMinutiae = trailingMinutiae.addAll(minutiaeList);
        STToken cloned = toClone.modifyWith(toClone.leadingMinutiae(), trailingMinutiae);
        return diagnosticCode == null ? cloned : addDiagnostic(cloned, diagnosticCode, args);
    }

    /**
     * Converts the invalid node into a list of {@code STMinutiae} nodes.
     * <p>
     * Here are the steps:
     * 1) Iterates through all the tokens in the invalid node. For each token:
     * 2) Add the leading minutiae to the list
     * 3) Create a new token without leading or trailing minutiae and add it to the list
     * 4) Add the trailing minutiae to the list
     *
     * @param invalidNode the invalid node to be converted
     * @return a lit of {@code STMinutiae} nodes
     */
    private static List<STNode> convertInvalidNodeToMinutiae(STNode invalidNode) {
        List<STNode> minutiaeList = new ArrayList<>();
        List<STToken> tokens = invalidNode.tokens();
        for (STToken token : tokens) {
            addMinutiaeToList(minutiaeList, token.leadingMinutiae());
            if (!token.isMissing()) {
                STToken tokenWithNoMinutiae = token.modifyWith(
                        STNodeFactory.createEmptyNodeList(), STNodeFactory.createEmptyNodeList());
                minutiaeList.add(STNodeFactory.createInvalidNodeMinutiae(tokenWithNoMinutiae));
            }
            addMinutiaeToList(minutiaeList, token.trailingMinutiae());
        }
        return minutiaeList;
    }

    private static void addMinutiaeToList(List<STNode> list, STNode minutiae) {
        if (!NodeListUtils.isSTNodeList(minutiae)) {
            list.add(minutiae);
            return;
        }

        STNodeList minutiaeList = (STNodeList) minutiae;
        for (int index = 0; index < minutiaeList.size(); index++) {
            STNode element = minutiaeList.get(index);
            if (SyntaxUtils.isSTNodePresent(element)) {
                list.add(element);
            }
        }
    }
}
