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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ImplicitNewExpressionNode extends NewExpressionNode {

    public ImplicitNewExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token newKeyword() {
        return childInBucket(0);
    }

    public Optional<ParenthesizedArgList> parenthesizedArgList() {
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
                "newKeyword",
                "parenthesizedArgList"};
    }

    public ImplicitNewExpressionNode modify(
            Token newKeyword,
            ParenthesizedArgList parenthesizedArgList) {
        if (checkForReferenceEquality(
                newKeyword,
                parenthesizedArgList)) {
            return this;
        }

        return NodeFactory.createImplicitNewExpressionNode(
                newKeyword,
                parenthesizedArgList);
    }

    public ImplicitNewExpressionNodeModifier modify() {
        return new ImplicitNewExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ImplicitNewExpressionNodeModifier {
        private final ImplicitNewExpressionNode oldNode;
        private Token NewKeyword;
        private ParenthesizedArgList ParenthesizedArgList;

        public ImplicitNewExpressionNodeModifier(ImplicitNewExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.NewKeyword = oldNode.NewKeyword();
            this.ParenthesizedArgList = oldNode.ParenthesizedArgList().orElse(null);
        }

        public ImplicitNewExpressionNodeModifier withNewKeyword(Token NewKeyword) {
            Objects.requireNonNull(NewKeyword, "NewKeyword must not be null");
            this.NewKeyword = NewKeyword;
            return this;
        }

        public ImplicitNewExpressionNodeModifier withParenthesizedArgList(ParenthesizedArgList ParenthesizedArgList) {
            Objects.requireNonNull(ParenthesizedArgList, "ParenthesizedArgList must not be null");
            this.ParenthesizedArgList = ParenthesizedArgList;
            return this;
        }

        public ImplicitNewExpressionNode apply() {
            return oldNode.modify(
                    NewKeyword,
                    ParenthesizedArgList);
        }
    }
}
