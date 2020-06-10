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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class CommitActionNode extends ActionNode {

    public CommitActionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token commitKeyword() {
        return childInBucket(0);
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
                "commitKeyword"};
    }

    public CommitActionNode modify(
            Token commitKeyword) {
        if (checkForReferenceEquality(
                commitKeyword)) {
            return this;
        }

        return NodeFactory.createCommitActionNode(
                commitKeyword);
    }

    public CommitActionNodeModifier modify() {
        return new CommitActionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class CommitActionNodeModifier {
        private final CommitActionNode oldNode;
        private Token commitKeyword;

        public CommitActionNodeModifier(CommitActionNode oldNode) {
            this.oldNode = oldNode;
            this.commitKeyword = oldNode.commitKeyword();
        }

        public CommitActionNodeModifier withCommitKeyword(
                Token commitKeyword) {
            Objects.requireNonNull(commitKeyword, "commitKeyword must not be null");
            this.commitKeyword = commitKeyword;
            return this;
        }

        public CommitActionNode apply() {
            return oldNode.modify(
                    commitKeyword);
        }
    }
}
