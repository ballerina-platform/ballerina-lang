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
public class GroupByClauseNode extends IntermediateClauseNode {

    public GroupByClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token groupKeyword() {
        return childInBucket(0);
    }

    public Token byKeyword() {
        return childInBucket(1);
    }

    public SeparatedNodeList<Node> groupingKey() {
        return new SeparatedNodeList<>(childInBucket(2));
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
                "groupKeyword",
                "byKeyword",
                "groupingKey"};
    }

    public GroupByClauseNode modify(
            Token groupKeyword,
            Token byKeyword,
            SeparatedNodeList<Node> groupingKey) {
        if (checkForReferenceEquality(
                groupKeyword,
                byKeyword,
                groupingKey.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createGroupByClauseNode(
                groupKeyword,
                byKeyword,
                groupingKey);
    }

    public GroupByClauseNodeModifier modify() {
        return new GroupByClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class GroupByClauseNodeModifier {
        private final GroupByClauseNode oldNode;
        private Token groupKeyword;
        private Token byKeyword;
        private SeparatedNodeList<Node> groupingKey;

        public GroupByClauseNodeModifier(GroupByClauseNode oldNode) {
            this.oldNode = oldNode;
            this.groupKeyword = oldNode.groupKeyword();
            this.byKeyword = oldNode.byKeyword();
            this.groupingKey = oldNode.groupingKey();
        }

        public GroupByClauseNodeModifier withGroupKeyword(
                Token groupKeyword) {
            Objects.requireNonNull(groupKeyword, "groupKeyword must not be null");
            this.groupKeyword = groupKeyword;
            return this;
        }

        public GroupByClauseNodeModifier withByKeyword(
                Token byKeyword) {
            Objects.requireNonNull(byKeyword, "byKeyword must not be null");
            this.byKeyword = byKeyword;
            return this;
        }

        public GroupByClauseNodeModifier withGroupingKey(
                SeparatedNodeList<Node> groupingKey) {
            Objects.requireNonNull(groupingKey, "groupingKey must not be null");
            this.groupingKey = groupingKey;
            return this;
        }

        public GroupByClauseNode apply() {
            return oldNode.modify(
                    groupKeyword,
                    byKeyword,
                    groupingKey);
        }
    }
}
