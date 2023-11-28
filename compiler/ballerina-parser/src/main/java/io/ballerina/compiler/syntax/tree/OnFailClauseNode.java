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

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class OnFailClauseNode extends ClauseNode {

    public OnFailClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token onKeyword() {
        return childInBucket(0);
    }

    public Token failKeyword() {
        return childInBucket(1);
    }

    public Optional<TypedBindingPatternNode> typedBindingPattern() {
        return optionalChildInBucket(2);
    }

    public BlockStatementNode blockStatement() {
        return childInBucket(3);
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
                "onKeyword",
                "failKeyword",
                "typedBindingPattern",
                "blockStatement"};
    }

    public OnFailClauseNode modify(
            Token onKeyword,
            Token failKeyword,
            TypedBindingPatternNode typedBindingPattern,
            BlockStatementNode blockStatement) {
        if (checkForReferenceEquality(
                onKeyword,
                failKeyword,
                typedBindingPattern,
                blockStatement)) {
            return this;
        }

        return NodeFactory.createOnFailClauseNode(
                onKeyword,
                failKeyword,
                typedBindingPattern,
                blockStatement);
    }

    public OnFailClauseNodeModifier modify() {
        return new OnFailClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OnFailClauseNodeModifier {
        private final OnFailClauseNode oldNode;
        private Token onKeyword;
        private Token failKeyword;
        private TypedBindingPatternNode typedBindingPattern;
        private BlockStatementNode blockStatement;

        public OnFailClauseNodeModifier(OnFailClauseNode oldNode) {
            this.oldNode = oldNode;
            this.onKeyword = oldNode.onKeyword();
            this.failKeyword = oldNode.failKeyword();
            this.typedBindingPattern = oldNode.typedBindingPattern().orElse(null);
            this.blockStatement = oldNode.blockStatement();
        }

        public OnFailClauseNodeModifier withOnKeyword(
                Token onKeyword) {
            Objects.requireNonNull(onKeyword, "onKeyword must not be null");
            this.onKeyword = onKeyword;
            return this;
        }

        public OnFailClauseNodeModifier withFailKeyword(
                Token failKeyword) {
            Objects.requireNonNull(failKeyword, "failKeyword must not be null");
            this.failKeyword = failKeyword;
            return this;
        }

        public OnFailClauseNodeModifier withTypedBindingPattern(
                TypedBindingPatternNode typedBindingPattern) {
            this.typedBindingPattern = typedBindingPattern;
            return this;
        }

        public OnFailClauseNodeModifier withBlockStatement(
                BlockStatementNode blockStatement) {
            Objects.requireNonNull(blockStatement, "blockStatement must not be null");
            this.blockStatement = blockStatement;
            return this;
        }

        public OnFailClauseNode apply() {
            return oldNode.modify(
                    onKeyword,
                    failKeyword,
                    typedBindingPattern,
                    blockStatement);
        }
    }
}
