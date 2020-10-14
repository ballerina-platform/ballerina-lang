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

    public static STNode createDocumentNode(
            STNode members,
            STNode eofToken) {

        return new STDocumentNode(
                members,
                eofToken);
    }

    public static STNode createTableNode(
            STNode openBracket,
            STNode identifier,
            STNode closeBracket,
            STNode fields) {

        return new STTableNode(
                openBracket,
                identifier,
                closeBracket,
                fields);
    }

    public static STNode createTableArrayNode(
            STNode firstOpenBracket,
            STNode secondOpenBracket,
            STNode identifier,
            STNode firstCloseBracket,
            STNode secondCloseBracket,
            STNode fields) {

        return new STTableArrayNode(
                firstOpenBracket,
                secondOpenBracket,
                identifier,
                firstCloseBracket,
                secondCloseBracket,
                fields);
    }

    public static STNode createKeyValueNode(
            STNode identifier,
            STNode assign,
            STNode value) {

        return new STKeyValueNode(
                identifier,
                assign,
                value);
    }

    public static STNode createArrayNode(
            STNode openBracket,
            STNode values,
            STNode closeBracket) {

        return new STArrayNode(
                openBracket,
                values,
                closeBracket);
    }

    public static STNode createStringLiteralNode(
            STNode startDoubleQuote,
            STNode content,
            STNode endDoubleQuote) {

        return new STStringLiteralNode(
                startDoubleQuote,
                content,
                endDoubleQuote);
    }

    public static STNode createNumericLiteralNode(
            SyntaxKind kind,
            STNode value) {

        return new STNumericLiteralNode(
                kind,
                value);
    }

    public static STNode createBoolLiteralNode(
            STNode value) {

        return new STBoolLiteralNode(
                value);
    }

    public static STNode createIdentifierLiteralNode(
            STNode value) {

        return new STIdentifierLiteralNode(
                value);
    }
}

