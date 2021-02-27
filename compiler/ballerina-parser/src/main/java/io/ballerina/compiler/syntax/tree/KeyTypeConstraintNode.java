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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class KeyTypeConstraintNode extends NonTerminalNode {

    public KeyTypeConstraintNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token keyKeywordToken() {
        return childInBucket(0);
    }

    public Node typeParameterNode() {
        return childInBucket(1);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "keyKeywordToken",
                "typeParameterNode"};
    }

    public KeyTypeConstraintNode modify(
            Token keyKeywordToken,
            Node typeParameterNode) {
        if (checkForReferenceEquality(
                keyKeywordToken,
                typeParameterNode)) {
            return this;
        }

        return NodeFactory.createKeyTypeConstraintNode(
                keyKeywordToken,
                typeParameterNode);
    }

    public KeyTypeConstraintNodeModifier modify() {
        return new KeyTypeConstraintNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class KeyTypeConstraintNodeModifier {
        private final KeyTypeConstraintNode oldNode;
        private Token keyKeywordToken;
        private Node typeParameterNode;

        public KeyTypeConstraintNodeModifier(KeyTypeConstraintNode oldNode) {
            this.oldNode = oldNode;
            this.keyKeywordToken = oldNode.keyKeywordToken();
            this.typeParameterNode = oldNode.typeParameterNode();
        }

        public KeyTypeConstraintNodeModifier withKeyKeywordToken(
                Token keyKeywordToken) {
            Objects.requireNonNull(keyKeywordToken, "keyKeywordToken must not be null");
            this.keyKeywordToken = keyKeywordToken;
            return this;
        }

        public KeyTypeConstraintNodeModifier withTypeParameterNode(
                Node typeParameterNode) {
            Objects.requireNonNull(typeParameterNode, "typeParameterNode must not be null");
            this.typeParameterNode = typeParameterNode;
            return this;
        }

        public KeyTypeConstraintNode apply() {
            return oldNode.modify(
                    keyKeywordToken,
                    typeParameterNode);
        }
    }
}
