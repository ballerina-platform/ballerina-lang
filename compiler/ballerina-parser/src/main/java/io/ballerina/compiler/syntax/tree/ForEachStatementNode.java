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
public class ForEachStatementNode extends StatementNode {

    public ForEachStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token forEachKeyword() {
        return childInBucket(0);
    }

    public TypedBindingPatternNode typedBindingPattern() {
        return childInBucket(1);
    }

    public Token inKeyword() {
        return childInBucket(2);
    }

    public Node actionOrExpressionNode() {
        return childInBucket(3);
    }

    public StatementNode blockStatement() {
        return childInBucket(4);
    }

    public Optional<OnFailClauseNode> onFailClause() {
        return optionalChildInBucket(5);
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
                "forEachKeyword",
                "typedBindingPattern",
                "inKeyword",
                "actionOrExpressionNode",
                "blockStatement",
                "onFailClause"};
    }

    public ForEachStatementNode modify(
            Token forEachKeyword,
            TypedBindingPatternNode typedBindingPattern,
            Token inKeyword,
            Node actionOrExpressionNode,
            StatementNode blockStatement,
            OnFailClauseNode onFailClause) {
        if (checkForReferenceEquality(
                forEachKeyword,
                typedBindingPattern,
                inKeyword,
                actionOrExpressionNode,
                blockStatement,
                onFailClause)) {
            return this;
        }

        return NodeFactory.createForEachStatementNode(
                forEachKeyword,
                typedBindingPattern,
                inKeyword,
                actionOrExpressionNode,
                blockStatement,
                onFailClause);
    }

    public ForEachStatementNodeModifier modify() {
        return new ForEachStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ForEachStatementNodeModifier {
        private final ForEachStatementNode oldNode;
        private Token forEachKeyword;
        private TypedBindingPatternNode typedBindingPattern;
        private Token inKeyword;
        private Node actionOrExpressionNode;
        private StatementNode blockStatement;
        private OnFailClauseNode onFailClause;

        public ForEachStatementNodeModifier(ForEachStatementNode oldNode) {
            this.oldNode = oldNode;
            this.forEachKeyword = oldNode.forEachKeyword();
            this.typedBindingPattern = oldNode.typedBindingPattern();
            this.inKeyword = oldNode.inKeyword();
            this.actionOrExpressionNode = oldNode.actionOrExpressionNode();
            this.blockStatement = oldNode.blockStatement();
            this.onFailClause = oldNode.onFailClause().orElse(null);
        }

        public ForEachStatementNodeModifier withForEachKeyword(
                Token forEachKeyword) {
            Objects.requireNonNull(forEachKeyword, "forEachKeyword must not be null");
            this.forEachKeyword = forEachKeyword;
            return this;
        }

        public ForEachStatementNodeModifier withTypedBindingPattern(
                TypedBindingPatternNode typedBindingPattern) {
            Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
            this.typedBindingPattern = typedBindingPattern;
            return this;
        }

        public ForEachStatementNodeModifier withInKeyword(
                Token inKeyword) {
            Objects.requireNonNull(inKeyword, "inKeyword must not be null");
            this.inKeyword = inKeyword;
            return this;
        }

        public ForEachStatementNodeModifier withActionOrExpressionNode(
                Node actionOrExpressionNode) {
            Objects.requireNonNull(actionOrExpressionNode, "actionOrExpressionNode must not be null");
            this.actionOrExpressionNode = actionOrExpressionNode;
            return this;
        }

        public ForEachStatementNodeModifier withBlockStatement(
                StatementNode blockStatement) {
            Objects.requireNonNull(blockStatement, "blockStatement must not be null");
            this.blockStatement = blockStatement;
            return this;
        }

        public ForEachStatementNodeModifier withOnFailClause(
                OnFailClauseNode onFailClause) {
            Objects.requireNonNull(onFailClause, "onFailClause must not be null");
            this.onFailClause = onFailClause;
            return this;
        }

        public ForEachStatementNode apply() {
            return oldNode.modify(
                    forEachKeyword,
                    typedBindingPattern,
                    inKeyword,
                    actionOrExpressionNode,
                    blockStatement,
                    onFailClause);
        }
    }
}
