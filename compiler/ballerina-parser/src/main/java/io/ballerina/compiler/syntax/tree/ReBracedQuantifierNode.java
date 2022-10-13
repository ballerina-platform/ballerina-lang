/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.3.0
 */
public class ReBracedQuantifierNode extends NonTerminalNode {

    public ReBracedQuantifierNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBraceToken() {
        return childInBucket(0);
    }

    public NodeList<Node> leastTimesMatchedDigit() {
        return new NodeList<>(childInBucket(1));
    }

    public Optional<Token> commaToken() {
        return optionalChildInBucket(2);
    }

    public NodeList<Node> mostTimesMatchedDigit() {
        return new NodeList<>(childInBucket(3));
    }

    public Token closeBraceToken() {
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
                "openBraceToken",
                "leastTimesMatchedDigit",
                "commaToken",
                "mostTimesMatchedDigit",
                "closeBraceToken"};
    }

    public ReBracedQuantifierNode modify(
            Token openBraceToken,
            NodeList<Node> leastTimesMatchedDigit,
            Token commaToken,
            NodeList<Node> mostTimesMatchedDigit,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                openBraceToken,
                leastTimesMatchedDigit.underlyingListNode(),
                commaToken,
                mostTimesMatchedDigit.underlyingListNode(),
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createReBracedQuantifierNode(
                openBraceToken,
                leastTimesMatchedDigit,
                commaToken,
                mostTimesMatchedDigit,
                closeBraceToken);
    }

    public ReBracedQuantifierNodeModifier modify() {
        return new ReBracedQuantifierNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReBracedQuantifierNodeModifier {
        private final ReBracedQuantifierNode oldNode;
        private Token openBraceToken;
        private NodeList<Node> leastTimesMatchedDigit;
        private Token commaToken;
        private NodeList<Node> mostTimesMatchedDigit;
        private Token closeBraceToken;

        public ReBracedQuantifierNodeModifier(ReBracedQuantifierNode oldNode) {
            this.oldNode = oldNode;
            this.openBraceToken = oldNode.openBraceToken();
            this.leastTimesMatchedDigit = oldNode.leastTimesMatchedDigit();
            this.commaToken = oldNode.commaToken().orElse(null);
            this.mostTimesMatchedDigit = oldNode.mostTimesMatchedDigit();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public ReBracedQuantifierNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public ReBracedQuantifierNodeModifier withLeastTimesMatchedDigit(
                NodeList<Node> leastTimesMatchedDigit) {
            Objects.requireNonNull(leastTimesMatchedDigit, "leastTimesMatchedDigit must not be null");
            this.leastTimesMatchedDigit = leastTimesMatchedDigit;
            return this;
        }

        public ReBracedQuantifierNodeModifier withCommaToken(
                Token commaToken) {
            this.commaToken = commaToken;
            return this;
        }

        public ReBracedQuantifierNodeModifier withMostTimesMatchedDigit(
                NodeList<Node> mostTimesMatchedDigit) {
            Objects.requireNonNull(mostTimesMatchedDigit, "mostTimesMatchedDigit must not be null");
            this.mostTimesMatchedDigit = mostTimesMatchedDigit;
            return this;
        }

        public ReBracedQuantifierNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public ReBracedQuantifierNode apply() {
            return oldNode.modify(
                    openBraceToken,
                    leastTimesMatchedDigit,
                    commaToken,
                    mostTimesMatchedDigit,
                    closeBraceToken);
        }
    }
}
