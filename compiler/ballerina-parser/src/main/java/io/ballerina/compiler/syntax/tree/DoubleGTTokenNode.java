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
public class DoubleGTTokenNode extends NonTerminalNode {

    public DoubleGTTokenNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openGTToken() {
        return childInBucket(0);
    }

    public Token endGTToken() {
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
                "openGTToken",
                "endGTToken"};
    }

    public DoubleGTTokenNode modify(
            Token openGTToken,
            Token endGTToken) {
        if (checkForReferenceEquality(
                openGTToken,
                endGTToken)) {
            return this;
        }

        return NodeFactory.createDoubleGTTokenNode(
                openGTToken,
                endGTToken);
    }

    public DoubleGTTokenNodeModifier modify() {
        return new DoubleGTTokenNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class DoubleGTTokenNodeModifier {
        private final DoubleGTTokenNode oldNode;
        private Token openGTToken;
        private Token endGTToken;

        public DoubleGTTokenNodeModifier(DoubleGTTokenNode oldNode) {
            this.oldNode = oldNode;
            this.openGTToken = oldNode.openGTToken();
            this.endGTToken = oldNode.endGTToken();
        }

        public DoubleGTTokenNodeModifier withOpenGTToken(
                Token openGTToken) {
            Objects.requireNonNull(openGTToken, "openGTToken must not be null");
            this.openGTToken = openGTToken;
            return this;
        }

        public DoubleGTTokenNodeModifier withEndGTToken(
                Token endGTToken) {
            Objects.requireNonNull(endGTToken, "endGTToken must not be null");
            this.endGTToken = endGTToken;
            return this;
        }

        public DoubleGTTokenNode apply() {
            return oldNode.modify(
                    openGTToken,
                    endGTToken);
        }
    }
}
