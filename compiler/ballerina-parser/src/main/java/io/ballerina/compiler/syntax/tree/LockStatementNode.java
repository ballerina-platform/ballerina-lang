/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class LockStatementNode extends StatementNode {

    public LockStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token lockKeyword() {
        return childInBucket(0);
    }

    public BlockStatementNode blockStatement() {
        return childInBucket(1);
    }

    public Optional<OnFailClauseNode> onFailClause() {
        return optionalChildInBucket(2);
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
                "lockKeyword",
                "blockStatement",
                "onFailClause"};
    }

    public LockStatementNode modify(
            Token lockKeyword,
            BlockStatementNode blockStatement,
            OnFailClauseNode onFailClause) {
        if (checkForReferenceEquality(
                lockKeyword,
                blockStatement,
                onFailClause)) {
            return this;
        }

        return NodeFactory.createLockStatementNode(
                lockKeyword,
                blockStatement,
                onFailClause);
    }

    public LockStatementNodeModifier modify() {
        return new LockStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class LockStatementNodeModifier {
        private final LockStatementNode oldNode;
        private Token lockKeyword;
        private BlockStatementNode blockStatement;
        @Nullable
        private OnFailClauseNode onFailClause;

        public LockStatementNodeModifier(LockStatementNode oldNode) {
            this.oldNode = oldNode;
            this.lockKeyword = oldNode.lockKeyword();
            this.blockStatement = oldNode.blockStatement();
            this.onFailClause = oldNode.onFailClause().orElse(null);
        }

        public LockStatementNodeModifier withLockKeyword(
                Token lockKeyword) {
            Objects.requireNonNull(lockKeyword, "lockKeyword must not be null");
            this.lockKeyword = lockKeyword;
            return this;
        }

        public LockStatementNodeModifier withBlockStatement(
                BlockStatementNode blockStatement) {
            Objects.requireNonNull(blockStatement, "blockStatement must not be null");
            this.blockStatement = blockStatement;
            return this;
        }

        public LockStatementNodeModifier withOnFailClause(
                OnFailClauseNode onFailClause) {
            this.onFailClause = onFailClause;
            return this;
        }

        public LockStatementNode apply() {
            return oldNode.modify(
                    lockKeyword,
                    blockStatement,
                    onFailClause);
        }
    }
}
