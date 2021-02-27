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
public class ElseBlockNode extends NonTerminalNode {

    public ElseBlockNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token elseKeyword() {
        return childInBucket(0);
    }

    public StatementNode elseBody() {
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
                "elseKeyword",
                "elseBody"};
    }

    public ElseBlockNode modify(
            Token elseKeyword,
            StatementNode elseBody) {
        if (checkForReferenceEquality(
                elseKeyword,
                elseBody)) {
            return this;
        }

        return NodeFactory.createElseBlockNode(
                elseKeyword,
                elseBody);
    }

    public ElseBlockNodeModifier modify() {
        return new ElseBlockNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ElseBlockNodeModifier {
        private final ElseBlockNode oldNode;
        private Token elseKeyword;
        private StatementNode elseBody;

        public ElseBlockNodeModifier(ElseBlockNode oldNode) {
            this.oldNode = oldNode;
            this.elseKeyword = oldNode.elseKeyword();
            this.elseBody = oldNode.elseBody();
        }

        public ElseBlockNodeModifier withElseKeyword(
                Token elseKeyword) {
            Objects.requireNonNull(elseKeyword, "elseKeyword must not be null");
            this.elseKeyword = elseKeyword;
            return this;
        }

        public ElseBlockNodeModifier withElseBody(
                StatementNode elseBody) {
            Objects.requireNonNull(elseBody, "elseBody must not be null");
            this.elseBody = elseBody;
            return this;
        }

        public ElseBlockNode apply() {
            return oldNode.modify(
                    elseKeyword,
                    elseBody);
        }
    }
}
