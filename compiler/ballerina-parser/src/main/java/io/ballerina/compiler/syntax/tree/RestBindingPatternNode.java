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
public class RestBindingPatternNode extends BindingPatternNode {

    public RestBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ellipsisToken() {
        return childInBucket(0);
    }

    public SimpleNameReferenceNode variableName() {
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
                "ellipsisToken",
                "variableName"};
    }

    public RestBindingPatternNode modify(
            Token ellipsisToken,
            SimpleNameReferenceNode variableName) {
        if (checkForReferenceEquality(
                ellipsisToken,
                variableName)) {
            return this;
        }

        return NodeFactory.createRestBindingPatternNode(
                ellipsisToken,
                variableName);
    }

    public RestBindingPatternNodeModifier modify() {
        return new RestBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RestBindingPatternNodeModifier {
        private final RestBindingPatternNode oldNode;
        private Token ellipsisToken;
        private SimpleNameReferenceNode variableName;

        public RestBindingPatternNodeModifier(RestBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.ellipsisToken = oldNode.ellipsisToken();
            this.variableName = oldNode.variableName();
        }

        public RestBindingPatternNodeModifier withEllipsisToken(
                Token ellipsisToken) {
            Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");
            this.ellipsisToken = ellipsisToken;
            return this;
        }

        public RestBindingPatternNodeModifier withVariableName(
                SimpleNameReferenceNode variableName) {
            Objects.requireNonNull(variableName, "variableName must not be null");
            this.variableName = variableName;
            return this;
        }

        public RestBindingPatternNode apply() {
            return oldNode.modify(
                    ellipsisToken,
                    variableName);
        }
    }
}
