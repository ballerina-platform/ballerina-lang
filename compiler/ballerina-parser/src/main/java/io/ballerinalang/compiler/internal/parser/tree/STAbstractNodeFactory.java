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
public abstract class STAbstractNodeFactory {

    public static STToken createIdentifierToken(String text, STNode leadingTrivia, STNode trailingTrivia) {
        return new STIdentifierToken(text, leadingTrivia, trailingTrivia);
    }

    public static STNode createNodeList(List<STNode> children) {
        return new STNodeList(children);
    }

    public static STNode createEmptyNode() {
        // TODO Seems like we can use a single instance of this node
        return new STEmptyNode();
    }

    public static STNode createMissingToken(SyntaxKind kind) {
        // TODO Seems like we can get these tokens from a cache
        return new STMissingToken(kind);
    }

    public static STToken createToken(SyntaxKind kind, STNode leadingTrivia, STNode trailingTrivia) {
        return new STToken(kind, leadingTrivia, trailingTrivia);
    }

    public static STToken createToken(SyntaxKind kind, int width, STNode leadingTrivia, STNode trailingTrivia) {
        return new STToken(kind, width, leadingTrivia, trailingTrivia);
    }

    public static STToken createTypeToken(SyntaxKind kind, String text, STNode leadingTrivia, STNode trailingTrivia) {
        return new STTypeToken(kind, text, leadingTrivia, trailingTrivia);
    }

    public static STToken createLiteralValueToken(SyntaxKind kind,
                                                  String text,
                                                  long value,
                                                  STNode leadingTrivia,
                                                  STNode trailingTrivia) {
        return new STLiteralValueToken(kind, text, value, leadingTrivia, trailingTrivia);
    }

    public static STNode createSyntaxTrivia(SyntaxKind kind, String text) {
        return new SyntaxTrivia(kind, text);
    }
}
