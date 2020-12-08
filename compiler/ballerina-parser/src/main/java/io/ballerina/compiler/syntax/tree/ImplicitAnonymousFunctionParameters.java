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
public class ImplicitAnonymousFunctionParameters extends NonTerminalNode {

    public ImplicitAnonymousFunctionParameters(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openParenToken() {
        return childInBucket(0);
    }

    public SeparatedNodeList<SimpleNameReferenceNode> parameters() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token closeParenToken() {
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
                "openParenToken",
                "parameters",
                "closeParenToken"};
    }

    public ImplicitAnonymousFunctionParameters modify(
            Token openParenToken,
            SeparatedNodeList<SimpleNameReferenceNode> parameters,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                openParenToken,
                parameters.underlyingListNode(),
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createImplicitAnonymousFunctionParameters(
                openParenToken,
                parameters,
                closeParenToken);
    }

    public ImplicitAnonymousFunctionParametersModifier modify() {
        return new ImplicitAnonymousFunctionParametersModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ImplicitAnonymousFunctionParametersModifier {
        private final ImplicitAnonymousFunctionParameters oldNode;
        private Token openParenToken;
        private SeparatedNodeList<SimpleNameReferenceNode> parameters;
        private Token closeParenToken;

        public ImplicitAnonymousFunctionParametersModifier(ImplicitAnonymousFunctionParameters oldNode) {
            this.oldNode = oldNode;
            this.openParenToken = oldNode.openParenToken();
            this.parameters = oldNode.parameters();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public ImplicitAnonymousFunctionParametersModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public ImplicitAnonymousFunctionParametersModifier withParameters(
                SeparatedNodeList<SimpleNameReferenceNode> parameters) {
            Objects.requireNonNull(parameters, "parameters must not be null");
            this.parameters = parameters;
            return this;
        }

        public ImplicitAnonymousFunctionParametersModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public ImplicitAnonymousFunctionParameters apply() {
            return oldNode.modify(
                    openParenToken,
                    parameters,
                    closeParenToken);
        }
    }
}
