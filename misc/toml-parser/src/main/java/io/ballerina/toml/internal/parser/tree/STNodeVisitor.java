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

import io.ballerina.toml.internal.syntax.SyntaxUtils;

/**
 * The {@code STNodeVisitor} visits each node in the internal syntax tree allowing
 * us to do something at each node.
 * <p>
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class STNodeVisitor {

    public void visit(STDocumentNode documentNode) {
        visitSyntaxNode(documentNode);
    }

    public void visit(STTableNode tableNode) {
        visitSyntaxNode(tableNode);
    }

    public void visit(STTableArrayNode tableArrayNode) {
        visitSyntaxNode(tableArrayNode);
    }

    public void visit(STKeyValueNode keyValueNode) {
        visitSyntaxNode(keyValueNode);
    }

    public void visit(STArrayNode arrayNode) {
        visitSyntaxNode(arrayNode);
    }

    public void visit(STStringLiteralNode stringLiteralNode) {
        visitSyntaxNode(stringLiteralNode);
    }

    public void visit(STNumericLiteralNode numericLiteralNode) {
        visitSyntaxNode(numericLiteralNode);
    }

    public void visit(STBoolLiteralNode boolLiteralNode) {
        visitSyntaxNode(boolLiteralNode);
    }

    public void visit(STIdentifierLiteralNode identifierLiteralNode) {
        visitSyntaxNode(identifierLiteralNode);
    }

    // STNodeList
    public void visit(STNodeList nodeList) {
        visitChildren(nodeList);
    }

    // Tokens

    public void visit(STToken token) {
    }

    // Misc

    protected void visitSyntaxNode(STNode node) {
        if (SyntaxUtils.isToken(node)) {
            node.accept(this);
            return;
        }

        visitChildren(node);
    }

    private void visitChildren(STNode node) {
        for (int bucket = 0; bucket < node.bucketCount(); bucket++) {
            STNode child = node.childInBucket(bucket);
            if (SyntaxUtils.isSTNodePresent(child)) {
                child.accept(this);
            }
        }
    }
}

