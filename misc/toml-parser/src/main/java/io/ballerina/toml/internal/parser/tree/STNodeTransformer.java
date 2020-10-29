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


/**
 * The {@code NodeTransformer} transform each node in the syntax tree to
 * another object of type T.
 * <p>
 * This is a generated class.
 *
 * @param <T> the type of class that is returned by visit methods
 * @since 2.0.0
 */
public abstract class STNodeTransformer<T> {

    public T transform(STDocumentNode documentNode) {
        return transformSyntaxNode(documentNode);
    }

    public T transform(STTableNode tableNode) {
        return transformSyntaxNode(tableNode);
    }

    public T transform(STTableArrayNode tableArrayNode) {
        return transformSyntaxNode(tableArrayNode);
    }

    public T transform(STKeyValueNode keyValueNode) {
        return transformSyntaxNode(keyValueNode);
    }

    public T transform(STArrayNode arrayNode) {
        return transformSyntaxNode(arrayNode);
    }

    public T transform(STStringLiteralNode stringLiteralNode) {
        return transformSyntaxNode(stringLiteralNode);
    }

    public T transform(STNumericLiteralNode numericLiteralNode) {
        return transformSyntaxNode(numericLiteralNode);
    }

    public T transform(STBoolLiteralNode boolLiteralNode) {
        return transformSyntaxNode(boolLiteralNode);
    }

    public T transform(STIdentifierLiteralNode identifierLiteralNode) {
        return transformSyntaxNode(identifierLiteralNode);
    }

    // Tokens

    public T transform(STToken token) {
        return null;
    }

    public T transform(STIdentifierToken identifier) {
        return transform((STToken) identifier);
    }

    public T transform(STLiteralValueToken literalValueToken) {
        return transform((STToken) literalValueToken);
    }

    public T transform(STDocumentationLineToken documentationLineToken) {
        return transform((STToken) documentationLineToken);
    }

    public T transform(STMissingToken missingToken) {
        return transform((STToken) missingToken);
    }

    // Misc

    public T transform(STNodeList nodeList) {
        return transformSyntaxNode(nodeList);
    }

    /**
     * Transforms the given {@code STNode} into an object of type T.
     * <p>
     * This method is invoked by each transform method in this class. You can
     * override it to provide a common transformation for each node.
     *
     * @param node the {@code STNode} to be transformed
     * @return the transformed object
     */
    protected abstract T transformSyntaxNode(STNode node);
}

