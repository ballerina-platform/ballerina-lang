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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class StreamTypeParamsNode extends NonTerminalNode {

    public StreamTypeParamsNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ltToken() {
        return childInBucket(0);
    }

    public Node leftTypeDescNode() {
        return childInBucket(1);
    }

    public Optional<Token> commaToken() {
        return optionalChildInBucket(2);
    }

    public Optional<Node> rightTypeDescNode() {
        return optionalChildInBucket(3);
    }

    public Token gtToken() {
        return childInBucket(4);
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
                "ltToken",
                "leftTypeDescNode",
                "commaToken",
                "rightTypeDescNode",
                "gtToken"};
    }

    public StreamTypeParamsNode modify(
            Token ltToken,
            Node leftTypeDescNode,
            Token commaToken,
            Node rightTypeDescNode,
            Token gtToken) {
        if (checkForReferenceEquality(
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken)) {
            return this;
        }

        return NodeFactory.createStreamTypeParamsNode(
                ltToken,
                leftTypeDescNode,
                commaToken,
                rightTypeDescNode,
                gtToken);
    }

    public StreamTypeParamsNodeModifier modify() {
        return new StreamTypeParamsNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class StreamTypeParamsNodeModifier {
        private final StreamTypeParamsNode oldNode;
        private Token ltToken;
        private Node leftTypeDescNode;
        private Token commaToken;
        private Node rightTypeDescNode;
        private Token gtToken;

        public StreamTypeParamsNodeModifier(StreamTypeParamsNode oldNode) {
            this.oldNode = oldNode;
            this.ltToken = oldNode.ltToken();
            this.leftTypeDescNode = oldNode.leftTypeDescNode();
            this.commaToken = oldNode.commaToken().orElse(null);
            this.rightTypeDescNode = oldNode.rightTypeDescNode().orElse(null);
            this.gtToken = oldNode.gtToken();
        }

        public StreamTypeParamsNodeModifier withLtToken(
                Token ltToken) {
            Objects.requireNonNull(ltToken, "ltToken must not be null");
            this.ltToken = ltToken;
            return this;
        }

        public StreamTypeParamsNodeModifier withLeftTypeDescNode(
                Node leftTypeDescNode) {
            Objects.requireNonNull(leftTypeDescNode, "leftTypeDescNode must not be null");
            this.leftTypeDescNode = leftTypeDescNode;
            return this;
        }

        public StreamTypeParamsNodeModifier withCommaToken(
                Token commaToken) {
            Objects.requireNonNull(commaToken, "commaToken must not be null");
            this.commaToken = commaToken;
            return this;
        }

        public StreamTypeParamsNodeModifier withRightTypeDescNode(
                Node rightTypeDescNode) {
            Objects.requireNonNull(rightTypeDescNode, "rightTypeDescNode must not be null");
            this.rightTypeDescNode = rightTypeDescNode;
            return this;
        }

        public StreamTypeParamsNodeModifier withGtToken(
                Token gtToken) {
            Objects.requireNonNull(gtToken, "gtToken must not be null");
            this.gtToken = gtToken;
            return this;
        }

        public StreamTypeParamsNode apply() {
            return oldNode.modify(
                    ltToken,
                    leftTypeDescNode,
                    commaToken,
                    rightTypeDescNode,
                    gtToken);
        }
    }
}
