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
public class ReFlagExpressionNode extends NonTerminalNode {

    public ReFlagExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token questionMark() {
        return childInBucket(0);
    }

    public ReFlagsOnOffNode reFlagsOnOff() {
        return childInBucket(1);
    }

    public Token colon() {
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
                "questionMark",
                "reFlagsOnOff",
                "colon"};
    }

    public ReFlagExpressionNode modify(
            Token questionMark,
            ReFlagsOnOffNode reFlagsOnOff,
            Token colon) {
        if (checkForReferenceEquality(
                questionMark,
                reFlagsOnOff,
                colon)) {
            return this;
        }

        return NodeFactory.createReFlagExpressionNode(
                questionMark,
                reFlagsOnOff,
                colon);
    }

    public ReFlagExpressionNodeModifier modify() {
        return new ReFlagExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReFlagExpressionNodeModifier {
        private final ReFlagExpressionNode oldNode;
        private Token questionMark;
        private ReFlagsOnOffNode reFlagsOnOff;
        private Token colon;

        public ReFlagExpressionNodeModifier(ReFlagExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.questionMark = oldNode.questionMark();
            this.reFlagsOnOff = oldNode.reFlagsOnOff();
            this.colon = oldNode.colon();
        }

        public ReFlagExpressionNodeModifier withQuestionMark(
                Token questionMark) {
            Objects.requireNonNull(questionMark, "questionMark must not be null");
            this.questionMark = questionMark;
            return this;
        }

        public ReFlagExpressionNodeModifier withReFlagsOnOff(
                ReFlagsOnOffNode reFlagsOnOff) {
            Objects.requireNonNull(reFlagsOnOff, "reFlagsOnOff must not be null");
            this.reFlagsOnOff = reFlagsOnOff;
            return this;
        }

        public ReFlagExpressionNodeModifier withColon(
                Token colon) {
            Objects.requireNonNull(colon, "colon must not be null");
            this.colon = colon;
            return this;
        }

        public ReFlagExpressionNode apply() {
            return oldNode.modify(
                    questionMark,
                    reFlagsOnOff,
                    colon);
        }
    }
}
