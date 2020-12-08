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
public class TrippleGTTokenNode extends NonTerminalNode {

    public TrippleGTTokenNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openGTToken() {
        return childInBucket(0);
    }

    public Token middleGTToken() {
        return childInBucket(1);
    }

    public Token endGTToken() {
        return childInBucket(2);
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
                "middleGTToken",
                "endGTToken"};
    }

    public TrippleGTTokenNode modify(
            Token openGTToken,
            Token middleGTToken,
            Token endGTToken) {
        if (checkForReferenceEquality(
                openGTToken,
                middleGTToken,
                endGTToken)) {
            return this;
        }

        return NodeFactory.createTrippleGTTokenNode(
                openGTToken,
                middleGTToken,
                endGTToken);
    }

    public TrippleGTTokenNodeModifier modify() {
        return new TrippleGTTokenNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TrippleGTTokenNodeModifier {
        private final TrippleGTTokenNode oldNode;
        private Token openGTToken;
        private Token middleGTToken;
        private Token endGTToken;

        public TrippleGTTokenNodeModifier(TrippleGTTokenNode oldNode) {
            this.oldNode = oldNode;
            this.openGTToken = oldNode.openGTToken();
            this.middleGTToken = oldNode.middleGTToken();
            this.endGTToken = oldNode.endGTToken();
        }

        public TrippleGTTokenNodeModifier withOpenGTToken(
                Token openGTToken) {
            Objects.requireNonNull(openGTToken, "openGTToken must not be null");
            this.openGTToken = openGTToken;
            return this;
        }

        public TrippleGTTokenNodeModifier withMiddleGTToken(
                Token middleGTToken) {
            Objects.requireNonNull(middleGTToken, "middleGTToken must not be null");
            this.middleGTToken = middleGTToken;
            return this;
        }

        public TrippleGTTokenNodeModifier withEndGTToken(
                Token endGTToken) {
            Objects.requireNonNull(endGTToken, "endGTToken must not be null");
            this.endGTToken = endGTToken;
            return this;
        }

        public TrippleGTTokenNode apply() {
            return oldNode.modify(
                    openGTToken,
                    middleGTToken,
                    endGTToken);
        }
    }
}
