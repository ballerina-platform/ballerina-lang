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
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.diagnostics.DiagnosticCode;
import io.ballerinalang.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains utilities to handle syntax errors.
 *
 * @since 2.0.0
 */
public class SyntaxErrors {

    public static <T extends STNode> T addDiagnostics(T node, DiagnosticCode... diagnosticCodes) {
        Collection<STNodeDiagnostic> diagnosticsToAdd = new ArrayList<>();
        for (DiagnosticCode diagnosticCode : diagnosticCodes) {
            diagnosticsToAdd.add(new STNodeDiagnostic(diagnosticCode));
        }
        return addSyntaxDiagnostics(node, diagnosticsToAdd);
    }

    public static <T extends STNode> T addDiagnostics(T node, Collection<DiagnosticCode> diagnosticCodes) {
        Collection<STNodeDiagnostic> diagnosticsToAdd = new ArrayList<>();
        for (DiagnosticCode diagnosticCode : diagnosticCodes) {
            diagnosticsToAdd.add(new STNodeDiagnostic(diagnosticCode));
        }
        return addSyntaxDiagnostics(node, diagnosticsToAdd);
    }

    public static <T extends STNode> T addSyntaxDiagnostics(T node, Collection<STNodeDiagnostic> diagnosticsToAdd) {
        if (diagnosticsToAdd.isEmpty()) {
            return node;
        }

        Collection<STNodeDiagnostic> newDiagnostics;
        Collection<STNodeDiagnostic> oldDiagnostics = node.diagnostics();
        if (oldDiagnostics.isEmpty()) {
            newDiagnostics = new ArrayList<>(diagnosticsToAdd);
        } else {
            // Merge all diagnostics
            newDiagnostics = new ArrayList<>(oldDiagnostics);
            newDiagnostics.addAll(diagnosticsToAdd);
        }
        return (T) node.modifyWith(newDiagnostics);
    }

    public static STToken createMissingToken(SyntaxKind expectedKind) {
        return STNodeFactory.createMissingToken(expectedKind);
    }

    public static STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind) {
        return createMissingTokenWithDiagnostics(expectedKind, getErrorCode(expectedKind));
    }

    public static STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind, DiagnosticCode diagnosticCode) {
        STNodeDiagnostic diagnostic = new STNodeDiagnostic(diagnosticCode);
        List<STNodeDiagnostic> diagnosticList = new ArrayList<>();
        diagnosticList.add(diagnostic);
        return STNodeFactory.createMissingToken(expectedKind, diagnosticList);
    }

    private static DiagnosticCode getErrorCode(SyntaxKind expectedKind) {
        switch (expectedKind) {
            case SEMICOLON_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_SEMICOLON_TOKEN;
            case COLON_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_COLON_TOKEN;
            case OPEN_PAREN_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_PAREN_TOKEN;
            case CLOSE_PAREN_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_PAREN_TOKEN;
            case OPEN_BRACE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACE_TOKEN;
            case CLOSE_BRACE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACE_TOKEN;
            case OPEN_BRACKET_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACKET_TOKEN;
            case CLOSE_BRACKET_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACKET_TOKEN;
            case EQUAL_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_EQUAL_TOKEN;
            case COMMA_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_COMMA_TOKEN;
            case PLUS_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_PLUS_TOKEN;
            case SLASH_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_SLASH_TOKEN;
            case AT_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_AT_TOKEN;
            case QUESTION_MARK_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_QUESTION_MARK_TOKEN;
            case GT_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_GT_TOKEN;
            case GT_EQUAL_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_GT_EQUAL_TOKEN;
            case LT_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_LT_TOKEN;
            case LT_EQUAL_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_LT_EQUAL_TOKEN;
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_RIGHT_DOUBLE_ARROW_TOKEN;
            case XML_COMMENT_END_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_XML_COMMENT_END_TOKEN;
            case XML_PI_END_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_XML_PI_END_TOKEN;
            case DOUBLE_QUOTE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_DOUBLE_QUOTE_TOKEN;
            case BACKTICK_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_BACKTICK_TOKEN;
            case OPEN_BRACE_PIPE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACE_PIPE_TOKEN;
            case CLOSE_BRACE_PIPE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACE_PIPE_TOKEN;
            case ASTERISK_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_ASTERISK_TOKEN;
            case PIPE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_PIPE_TOKEN;
            case DOT_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_DOT_TOKEN;

            case DEFAULT_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_DEFAULT_KEYWORD;
            case TYPE_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_TYPE_KEYWORD;
            case ON_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_ON_KEYWORD;
            case ANNOTATION_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_ANNOTATION_KEYWORD;
            case FUNCTION_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_FUNCTION_KEYWORD;
            case SOURCE_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_SOURCE_KEYWORD;
            case ENUM_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_ENUM_KEYWORD;
            case FIELD_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_FIELD_KEYWORD;
            case VERSION_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_VERSION_KEYWORD;
            case OBJECT_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_OBJECT_KEYWORD;
            case RECORD_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_RECORD_KEYWORD;
            case SERVICE_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_SERVICE_KEYWORD;
            case AS_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_AS_KEYWORD;
            case LET_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_LET_KEYWORD;
            case TABLE_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_TABLE_KEYWORD;
            case KEY_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_KEY_KEYWORD;
            case FROM_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_FROM_KEYWORD;
            case IN_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_IN_KEYWORD;
            case IF_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_IF_KEYWORD;
            case IMPORT_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_IMPORT_KEYWORD;
            case CONST_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_CONST_KEYWORD;
            case EXTERNAL_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_EXTERNAL_KEYWORD;

            case IDENTIFIER_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER;
            case DECIMAL_INTEGER_LITERAL:
                return DiagnosticErrorCode.ERROR_MISSING_DECIMAL_INTEGER_LITERAL;
            case TYPE_DESC:
                return DiagnosticErrorCode.ERROR_MISSING_TYPE_DESC;
            default:
                throw new UnsupportedOperationException("Unsupported SyntaxKind: " + expectedKind);
        }
    }

    /**
     * Clone the given {@code STNode} with the invalid node as leading minutiae.
     *
     * @param toClone     the node to be cloned
     * @param invalidNode the invalid
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithLeadingInvalidNodeMinutiae(STNode toClone, STNode invalidNode) {
        STToken firstToken = toClone.firstToken();
        STToken firstTokenWithInvalidNodeMinutiae = cloneWithLeadingInvalidNodeMinutiae(firstToken,
                invalidNode, new DiagnosticCode[0]);
        return toClone.replace(firstToken, firstTokenWithInvalidNodeMinutiae);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as leading minutiae.
     *
     * @param toClone         the node to be cloned
     * @param invalidNode     the invalid
     * @param diagnosticCodes the list of diagnostics to be added
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithLeadingInvalidNodeMinutiae(STNode toClone,
                                                             STNode invalidNode,
                                                             DiagnosticCode... diagnosticCodes) {
        STToken firstToken = toClone.firstToken();
        STToken firstTokenWithInvalidNodeMinutiae = cloneWithLeadingInvalidNodeMinutiae(firstToken,
                invalidNode, diagnosticCodes);
        return toClone.replace(firstToken, firstTokenWithInvalidNodeMinutiae);
    }

    /**
     * Clone the given {@code STToken} with the invalid node as leading minutiae.
     *
     * @param toClone         the token to be cloned
     * @param invalidNode     the invalid
     * @param diagnosticCodes the list of diagnostics to be added
     * @return a cloned token with the given invalidNode minutiae
     */
    public static STToken cloneWithLeadingInvalidNodeMinutiae(STToken toClone,
                                                              STNode invalidNode,
                                                              DiagnosticCode... diagnosticCodes) {
        STNode invalidNodeMinutiae = STNodeFactory.createInvalidNodeMinutiae(invalidNode);
        STNodeList leadingMinutiae = (STNodeList) toClone.leadingMinutiae();
        leadingMinutiae = leadingMinutiae.add(0, invalidNodeMinutiae);
        STToken cloned = toClone.modifyWith(leadingMinutiae, toClone.trailingMinutiae());
        return addDiagnostics(cloned, diagnosticCodes);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as trailing minutiae.
     *
     * @param toClone     the node to be cloned
     * @param invalidNode the invalid
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithTrailingInvalidNodeMinutiae(STNode toClone, STNode invalidNode) {
        STToken lastToken = toClone.lastToken();
        STToken lastTokenWithInvalidNodeMinutiae = cloneWithTrailingInvalidNodeMinutiae(lastToken,
                invalidNode, new DiagnosticCode[0]);
        return toClone.replace(lastToken, lastTokenWithInvalidNodeMinutiae);
    }

    /**
     * Clone the given {@code STNode} with the invalid node as trailing minutiae.
     *
     * @param toClone         the node to be cloned
     * @param invalidNode     the invalid
     * @param diagnosticCodes the list of diagnostics to be added
     * @return a cloned node with the given invalidNode minutiae
     */
    public static STNode cloneWithTrailingInvalidNodeMinutiae(STNode toClone,
                                                              STNode invalidNode,
                                                              DiagnosticCode... diagnosticCodes) {
        STToken lastToken = toClone.lastToken();
        STToken lastTokenWithInvalidNodeMinutiae = cloneWithTrailingInvalidNodeMinutiae(lastToken,
                invalidNode, diagnosticCodes);
        return toClone.replace(lastToken, lastTokenWithInvalidNodeMinutiae);
    }

    /**
     * Clone the given {@code STToken} with the invalid node as trailing minutiae.
     *
     * @param toClone         the token to be cloned
     * @param invalidNode     the invalid
     * @param diagnosticCodes the list of diagnostics to be added
     * @return a cloned token with the given invalidNode minutiae
     */
    public static STToken cloneWithTrailingInvalidNodeMinutiae(STToken toClone,
                                                               STNode invalidNode,
                                                               DiagnosticCode... diagnosticCodes) {
        STNode invalidNodeMinutiae = STNodeFactory.createInvalidNodeMinutiae(invalidNode);
        STNodeList trailingMinutiae = (STNodeList) toClone.trailingMinutiae();
        trailingMinutiae = trailingMinutiae.add(invalidNodeMinutiae);
        STToken cloned = toClone.modifyWith(toClone.leadingMinutiae(), trailingMinutiae);
        return addDiagnostics(cloned, diagnosticCodes);
    }
}
