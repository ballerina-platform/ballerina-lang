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
public class ReAtomQuantifierNode extends ReTermNode {

    public ReAtomQuantifierNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ReAtomNode reAtom() {
        return childInBucket(0);
    }

    public Optional<ReQuantifierNode> reQuantifier() {
        return optionalChildInBucket(1);
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
                "reAtom",
                "reQuantifier"};
    }

    public ReAtomQuantifierNode modify(
            ReAtomNode reAtom,
            ReQuantifierNode reQuantifier) {
        if (checkForReferenceEquality(
                reAtom,
                reQuantifier)) {
            return this;
        }

        return NodeFactory.createReAtomQuantifierNode(
                reAtom,
                reQuantifier);
    }

    public ReAtomQuantifierNodeModifier modify() {
        return new ReAtomQuantifierNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReAtomQuantifierNodeModifier {
        private final ReAtomQuantifierNode oldNode;
        private ReAtomNode reAtom;
        private ReQuantifierNode reQuantifier;

        public ReAtomQuantifierNodeModifier(ReAtomQuantifierNode oldNode) {
            this.oldNode = oldNode;
            this.reAtom = oldNode.reAtom();
            this.reQuantifier = oldNode.reQuantifier().orElse(null);
        }

        public ReAtomQuantifierNodeModifier withReAtom(
                ReAtomNode reAtom) {
            Objects.requireNonNull(reAtom, "reAtom must not be null");
            this.reAtom = reAtom;
            return this;
        }

        public ReAtomQuantifierNodeModifier withReQuantifier(
                ReQuantifierNode reQuantifier) {
            this.reQuantifier = reQuantifier;
            return this;
        }

        public ReAtomQuantifierNode apply() {
            return oldNode.modify(
                    reAtom,
                    reQuantifier);
        }
    }
}
