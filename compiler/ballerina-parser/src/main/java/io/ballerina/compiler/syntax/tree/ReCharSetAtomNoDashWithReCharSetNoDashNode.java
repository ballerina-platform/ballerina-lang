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
public class ReCharSetAtomNoDashWithReCharSetNoDashNode extends NonTerminalNode {

    public ReCharSetAtomNoDashWithReCharSetNoDashNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node reCharSetAtomNoDash() {
        return childInBucket(0);
    }

    public Node reCharSetNoDash() {
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
                "reCharSetAtomNoDash",
                "reCharSetNoDash"};
    }

    public ReCharSetAtomNoDashWithReCharSetNoDashNode modify(
            Node reCharSetAtomNoDash,
            Node reCharSetNoDash) {
        if (checkForReferenceEquality(
                reCharSetAtomNoDash,
                reCharSetNoDash)) {
            return this;
        }

        return NodeFactory.createReCharSetAtomNoDashWithReCharSetNoDashNode(
                reCharSetAtomNoDash,
                reCharSetNoDash);
    }

    public ReCharSetAtomNoDashWithReCharSetNoDashNodeModifier modify() {
        return new ReCharSetAtomNoDashWithReCharSetNoDashNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReCharSetAtomNoDashWithReCharSetNoDashNodeModifier {
        private final ReCharSetAtomNoDashWithReCharSetNoDashNode oldNode;
        private Node reCharSetAtomNoDash;
        private Node reCharSetNoDash;

        public ReCharSetAtomNoDashWithReCharSetNoDashNodeModifier(ReCharSetAtomNoDashWithReCharSetNoDashNode oldNode) {
            this.oldNode = oldNode;
            this.reCharSetAtomNoDash = oldNode.reCharSetAtomNoDash();
            this.reCharSetNoDash = oldNode.reCharSetNoDash();
        }

        public ReCharSetAtomNoDashWithReCharSetNoDashNodeModifier withReCharSetAtomNoDash(
                Node reCharSetAtomNoDash) {
            Objects.requireNonNull(reCharSetAtomNoDash, "reCharSetAtomNoDash must not be null");
            this.reCharSetAtomNoDash = reCharSetAtomNoDash;
            return this;
        }

        public ReCharSetAtomNoDashWithReCharSetNoDashNodeModifier withReCharSetNoDash(
                Node reCharSetNoDash) {
            Objects.requireNonNull(reCharSetNoDash, "reCharSetNoDash must not be null");
            this.reCharSetNoDash = reCharSetNoDash;
            return this;
        }

        public ReCharSetAtomNoDashWithReCharSetNoDashNode apply() {
            return oldNode.modify(
                    reCharSetAtomNoDash,
                    reCharSetNoDash);
        }
    }
}
