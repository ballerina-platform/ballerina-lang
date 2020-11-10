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
package io.ballerina.toml.internal.parser.tree;

import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.Collection;

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
public abstract class STAbstractNodeFactory {

    private static final STNodeList EMPTY_LIST = new STNodeList();

    public static STToken createIdentifierToken(String text, STNode leadingTrivia, STNode trailingTrivia) {
        return new STIdentifierToken(text, leadingTrivia, trailingTrivia);
    }

    public static STToken createIdentifierToken(String text,
                                                STNode leadingTrivia,
                                                STNode trailingTrivia,
                                                Collection<STNodeDiagnostic> diagnostics) {
        return new STIdentifierToken(text, leadingTrivia, trailingTrivia, diagnostics);
    }

    public static STNode createNodeList(Collection<STNode> children) {
        if (children.isEmpty()) {
            return EMPTY_LIST;
        }
        return new STNodeList(children);
    }

    public static STNode createNodeList(
            STNode... children) {
        if (children.length == 0) {
            return EMPTY_LIST;
        }
        return new STNodeList(children);
    }

    public static STNode createEmptyNodeList() {
        return EMPTY_LIST;
    }

    public static STNode createEmptyNode() {
        return null;
    }

    public static STToken createMissingToken(SyntaxKind kind) {
        // TODO Seems like we can get these tokens from a cache
        return new STMissingToken(kind);
    }

    public static STToken createMissingToken(SyntaxKind kind, Collection<STNodeDiagnostic> diagnostics) {
        // TODO Seems like we can get these tokens from a cache
        return new STMissingToken(kind, diagnostics);
    }

    public static STToken createInvalidToken(String tokenText) {
        return new STInvalidToken(tokenText);
    }

    public static STToken createToken(SyntaxKind kind, STNode leadingTrivia, STNode trailingTrivia) {
        return new STToken(kind, leadingTrivia, trailingTrivia);
    }

    public static STToken createToken(SyntaxKind kind,
                                      STNode leadingTrivia,
                                      STNode trailingTrivia,
                                      Collection<STNodeDiagnostic> diagnostics) {
        return new STToken(kind, kind.stringValue().length(), leadingTrivia, trailingTrivia, diagnostics);
    }

    public static STToken createLiteralValueToken(SyntaxKind kind,
                                                  String text,
                                                  STNode leadingTrivia,
                                                  STNode trailingTrivia) {
        return new STLiteralValueToken(kind, text, leadingTrivia, trailingTrivia);
    }

    public static STToken createLiteralValueToken(SyntaxKind kind,
                                                  String text,
                                                  STNode leadingTrivia,
                                                  STNode trailingTrivia,
                                                  Collection<STNodeDiagnostic> diagnostics) {
        return new STLiteralValueToken(kind, text, leadingTrivia, trailingTrivia, diagnostics);
    }

    public static STNode createMinutiae(SyntaxKind kind, String text) {
        return new STMinutiae(kind, text);
    }

    /**
     * Create a Minutia node with the given text and the width.
     * <p>
     * This method allows the lexer to set the width that is different from the text length.
     *
     * @param kind  the {@code SyntaxKind}
     * @param text  the lexeme
     * @param width the width of the lexeme
     * @return the Minutia node
     */
    public static STNode createMinutiae(SyntaxKind kind, String text, int width) {
        return new STMinutiae(kind, text, width);
    }

    public static STNode createInvalidNodeMinutiae(
            STNode invalidNode) {
        return new STInvalidNodeMinutiae(invalidNode);
    }

    public static STToken createDocumentationLineToken(String text, STNode leadingTrivia, STNode trailingTrivia) {
        return new STDocumentationLineToken(text, leadingTrivia, trailingTrivia);
    }

    public static STToken createDocumentationLineToken(String text,
                                                       STNode leadingTrivia,
                                                       STNode trailingTrivia,
                                                       Collection<STNodeDiagnostic> diagnostics) {
        return new STDocumentationLineToken(text, leadingTrivia, trailingTrivia, diagnostics);
    }
}
