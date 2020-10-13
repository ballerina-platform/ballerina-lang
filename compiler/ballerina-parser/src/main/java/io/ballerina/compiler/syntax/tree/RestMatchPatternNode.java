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
public class RestMatchPatternNode extends NonTerminalNode {

    public RestMatchPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ellipsisToken() {
        return childInBucket(0);
    }

    public Token varKeywordToken() {
        return childInBucket(1);
    }

    public SimpleNameReferenceNode variableName() {
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
                "ellipsisToken",
                "varKeywordToken",
                "variableName"};
    }

    public RestMatchPatternNode modify(
            Token ellipsisToken,
            Token varKeywordToken,
            SimpleNameReferenceNode variableName) {
        if (checkForReferenceEquality(
                ellipsisToken,
                varKeywordToken,
                variableName)) {
            return this;
        }

        return NodeFactory.createRestMatchPatternNode(
                ellipsisToken,
                varKeywordToken,
                variableName);
    }

    public RestMatchPatternNodeModifier modify() {
        return new RestMatchPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RestMatchPatternNodeModifier {
        private final RestMatchPatternNode oldNode;
        private Token ellipsisToken;
        private Token varKeywordToken;
        private SimpleNameReferenceNode variableName;

        public RestMatchPatternNodeModifier(RestMatchPatternNode oldNode) {
            this.oldNode = oldNode;
            this.ellipsisToken = oldNode.ellipsisToken();
            this.varKeywordToken = oldNode.varKeywordToken();
            this.variableName = oldNode.variableName();
        }

        public RestMatchPatternNodeModifier withEllipsisToken(
                Token ellipsisToken) {
            Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");
            this.ellipsisToken = ellipsisToken;
            return this;
        }

        public RestMatchPatternNodeModifier withVarKeywordToken(
                Token varKeywordToken) {
            Objects.requireNonNull(varKeywordToken, "varKeywordToken must not be null");
            this.varKeywordToken = varKeywordToken;
            return this;
        }

        public RestMatchPatternNodeModifier withVariableName(
                SimpleNameReferenceNode variableName) {
            Objects.requireNonNull(variableName, "variableName must not be null");
            this.variableName = variableName;
            return this;
        }

        public RestMatchPatternNode apply() {
            return oldNode.modify(
                    ellipsisToken,
                    varKeywordToken,
                    variableName);
        }
    }
}
