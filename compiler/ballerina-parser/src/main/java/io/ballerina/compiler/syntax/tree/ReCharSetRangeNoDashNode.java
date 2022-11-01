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

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.3.0
 */
public class ReCharSetRangeNoDashNode extends NonTerminalNode {

    public ReCharSetRangeNoDashNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node reCharSetAtomNoDash() {
        return childInBucket(0);
    }

    public Token minusToken() {
        return childInBucket(1);
    }

    public Node reCharSetAtom() {
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
                "reCharSetAtomNoDash",
                "minusToken",
                "reCharSetAtom"};
    }

    public ReCharSetRangeNoDashNode modify(
            Node reCharSetAtomNoDash,
            Token minusToken,
            Node reCharSetAtom) {
        if (checkForReferenceEquality(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom)) {
            return this;
        }

        return NodeFactory.createReCharSetRangeNoDashNode(
                reCharSetAtomNoDash,
                minusToken,
                reCharSetAtom);
    }

    public ReCharSetRangeNoDashNodeModifier modify() {
        return new ReCharSetRangeNoDashNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReCharSetRangeNoDashNodeModifier {
        private final ReCharSetRangeNoDashNode oldNode;
        private Node reCharSetAtomNoDash;
        private Token minusToken;
        private Node reCharSetAtom;

        public ReCharSetRangeNoDashNodeModifier(ReCharSetRangeNoDashNode oldNode) {
            this.oldNode = oldNode;
            this.reCharSetAtomNoDash = oldNode.reCharSetAtomNoDash();
            this.minusToken = oldNode.minusToken();
            this.reCharSetAtom = oldNode.reCharSetAtom();
        }

        public ReCharSetRangeNoDashNodeModifier withReCharSetAtomNoDash(
                Node reCharSetAtomNoDash) {
            Objects.requireNonNull(reCharSetAtomNoDash, "reCharSetAtomNoDash must not be null");
            this.reCharSetAtomNoDash = reCharSetAtomNoDash;
            return this;
        }

        public ReCharSetRangeNoDashNodeModifier withMinusToken(
                Token minusToken) {
            Objects.requireNonNull(minusToken, "minusToken must not be null");
            this.minusToken = minusToken;
            return this;
        }

        public ReCharSetRangeNoDashNodeModifier withReCharSetAtom(
                Node reCharSetAtom) {
            Objects.requireNonNull(reCharSetAtom, "reCharSetAtom must not be null");
            this.reCharSetAtom = reCharSetAtom;
            return this;
        }

        public ReCharSetRangeNoDashNode apply() {
            return oldNode.modify(
                    reCharSetAtomNoDash,
                    minusToken,
                    reCharSetAtom);
        }
    }
}
