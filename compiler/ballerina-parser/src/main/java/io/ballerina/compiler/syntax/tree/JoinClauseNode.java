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
public class JoinClauseNode extends IntermediateClauseNode {

    public JoinClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> outerKeyword() {
        return optionalChildInBucket(0);
    }

    public Token joinKeyword() {
        return childInBucket(1);
    }

    public TypedBindingPatternNode typedBindingPattern() {
        return childInBucket(2);
    }

    public Token inKeyword() {
        return childInBucket(3);
    }

    public ExpressionNode expression() {
        return childInBucket(4);
    }

    public OnClauseNode joinOnCondition() {
        return childInBucket(5);
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
                "outerKeyword",
                "joinKeyword",
                "typedBindingPattern",
                "inKeyword",
                "expression",
                "joinOnCondition"};
    }

    public JoinClauseNode modify(
            Token outerKeyword,
            Token joinKeyword,
            TypedBindingPatternNode typedBindingPattern,
            Token inKeyword,
            ExpressionNode expression,
            OnClauseNode joinOnCondition) {
        if (checkForReferenceEquality(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition)) {
            return this;
        }

        return NodeFactory.createJoinClauseNode(
                outerKeyword,
                joinKeyword,
                typedBindingPattern,
                inKeyword,
                expression,
                joinOnCondition);
    }

    public JoinClauseNodeModifier modify() {
        return new JoinClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class JoinClauseNodeModifier {
        private final JoinClauseNode oldNode;
        private Token outerKeyword;
        private Token joinKeyword;
        private TypedBindingPatternNode typedBindingPattern;
        private Token inKeyword;
        private ExpressionNode expression;
        private OnClauseNode joinOnCondition;

        public JoinClauseNodeModifier(JoinClauseNode oldNode) {
            this.oldNode = oldNode;
            this.outerKeyword = oldNode.outerKeyword().orElse(null);
            this.joinKeyword = oldNode.joinKeyword();
            this.typedBindingPattern = oldNode.typedBindingPattern();
            this.inKeyword = oldNode.inKeyword();
            this.expression = oldNode.expression();
            this.joinOnCondition = oldNode.joinOnCondition();
        }

        public JoinClauseNodeModifier withOuterKeyword(
                Token outerKeyword) {
            Objects.requireNonNull(outerKeyword, "outerKeyword must not be null");
            this.outerKeyword = outerKeyword;
            return this;
        }

        public JoinClauseNodeModifier withJoinKeyword(
                Token joinKeyword) {
            Objects.requireNonNull(joinKeyword, "joinKeyword must not be null");
            this.joinKeyword = joinKeyword;
            return this;
        }

        public JoinClauseNodeModifier withTypedBindingPattern(
                TypedBindingPatternNode typedBindingPattern) {
            Objects.requireNonNull(typedBindingPattern, "typedBindingPattern must not be null");
            this.typedBindingPattern = typedBindingPattern;
            return this;
        }

        public JoinClauseNodeModifier withInKeyword(
                Token inKeyword) {
            Objects.requireNonNull(inKeyword, "inKeyword must not be null");
            this.inKeyword = inKeyword;
            return this;
        }

        public JoinClauseNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public JoinClauseNodeModifier withJoinOnCondition(
                OnClauseNode joinOnCondition) {
            Objects.requireNonNull(joinOnCondition, "joinOnCondition must not be null");
            this.joinOnCondition = joinOnCondition;
            return this;
        }

        public JoinClauseNode apply() {
            return oldNode.modify(
                    outerKeyword,
                    joinKeyword,
                    typedBindingPattern,
                    inKeyword,
                    expression,
                    joinOnCondition);
        }
    }
}
