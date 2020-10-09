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
package io.ballerina.toml.syntax.tree;


/**
 * The {@code NodeTransformer} transform each node in the syntax tree to
 * another object of type T.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * This class allows you to transform the syntax tree into something else without
 * mutating instance variables.
 * <p>
 * There exists a transform method for each node in the Ballerina syntax tree.
 * These methods return T. If you are looking for a visitor that has visit
 * methods that return void, see {@link NodeVisitor}.
 *
 * This is a generated class.
 *
 * @param <T> the type of class that is returned by visit methods
 * @see NodeVisitor
 * @since 2.0.0
 */
public abstract class NodeTransformer<T> {

    public T transform(DocumentNode documentNode) {
        return transformSyntaxNode(documentNode);
    }

    public T transform(TableNode tableNode) {
        return transformSyntaxNode(tableNode);
    }

    public T transform(TableArrayNode tableArrayNode) {
        return transformSyntaxNode(tableArrayNode);
    }

    public T transform(KeyValueNode keyValueNode) {
        return transformSyntaxNode(keyValueNode);
    }

    public T transform(ArrayNode arrayNode) {
        return transformSyntaxNode(arrayNode);
    }

    public T transform(StringLiteralNode stringLiteralNode) {
        return transformSyntaxNode(stringLiteralNode);
    }

    public T transform(NumericLiteralNode numericLiteralNode) {
        return transformSyntaxNode(numericLiteralNode);
    }

    public T transform(BoolLiteralNode boolLiteralNode) {
        return transformSyntaxNode(boolLiteralNode);
    }

    public T transform(IdentifierLiteralNode identifierLiteralNode) {
        return transformSyntaxNode(identifierLiteralNode);
    }

    // Tokens

    public T transform(Token token) {
        return null;
    }

    public T transform(IdentifierToken identifier) {
        return transform((Token) identifier);
    }

    // Misc

    /**
     * Transforms the given {@code Node} into an object of type T.
     * <p>
     * This method is invoked by each transform method in this class. You can
     * override it to provide a common transformation for each node.
     *
     * @param node the {@code Node} to be transformed
     * @return the transformed object
     */
    protected abstract T transformSyntaxNode(Node node);
}

