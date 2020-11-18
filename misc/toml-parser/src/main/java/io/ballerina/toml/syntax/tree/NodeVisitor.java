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
 * The {@code NodeVisitor} visits each node in the syntax tree allowing
 * us to do something at each node.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * {@code NodeVisitor} is a abstract class that itself visits the complete
 * tree. Subclasses have the ability to override only the required visit methods.
 * <p>
 * There exists a visit method for each node in the Ballerina syntax tree.
 * These methods return void. If you are looking for a visitor that has visit
 * methods that returns something, see {@link NodeTransformer}.
 *
 * This is a generated class.
 *
 * @see NodeTransformer
 * @since 2.0.0
 */
public abstract class NodeVisitor {

    public void visit(DocumentNode documentNode) {
        visitSyntaxNode(documentNode);
    }

    public void visit(TableNode tableNode) {
        visitSyntaxNode(tableNode);
    }

    public void visit(TableArrayNode tableArrayNode) {
        visitSyntaxNode(tableArrayNode);
    }

    public void visit(KeyValueNode keyValueNode) {
        visitSyntaxNode(keyValueNode);
    }

    public void visit(ArrayNode arrayNode) {
        visitSyntaxNode(arrayNode);
    }

    public void visit(StringLiteralNode stringLiteralNode) {
        visitSyntaxNode(stringLiteralNode);
    }

    public void visit(NumericLiteralNode numericLiteralNode) {
        visitSyntaxNode(numericLiteralNode);
    }

    public void visit(BoolLiteralNode boolLiteralNode) {
        visitSyntaxNode(boolLiteralNode);
    }

    public void visit(IdentifierLiteralNode identifierLiteralNode) {
        visitSyntaxNode(identifierLiteralNode);
    }

    // Tokens

    public void visit(Token token) {
    }

    // Misc

    protected void visitSyntaxNode(Node node) {
        // TODO Find a better way to check for token
        if (node instanceof Token) {
            node.accept(this);
            return;
        }

        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        for (Node child : nonTerminalNode.children()) {
            child.accept(this);
        }
    }
}

