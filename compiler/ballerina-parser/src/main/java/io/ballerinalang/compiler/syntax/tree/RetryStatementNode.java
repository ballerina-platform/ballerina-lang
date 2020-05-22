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
public class RetryStatementNode extends StatementNode {

    public RetryStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token retryKeyword() {
        return childInBucket(0);
    }

    public Optional<TypeParameterNode> typeParameter() {
        return optionalChildInBucket(1);
    }

    public Optional<ParenthesizedArgList> arguments() {
        return optionalChildInBucket(2);
    }

    public StatementNode retryBody() {
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
                "retryKeyword",
                "typeParameter",
                "arguments",
                "retryBody"};
    }

    public RetryStatementNode modify(
            Token retryKeyword,
            TypeParameterNode typeParameter,
            ParenthesizedArgList arguments,
            StatementNode retryBody) {
        if (checkForReferenceEquality(
                retryKeyword,
                typeParameter,
                arguments,
                retryBody)) {
            return this;
        }

        return NodeFactory.createRetryStatementNode(
                retryKeyword,
                typeParameter,
                arguments,
                retryBody);
    }

    public RetryStatementNodeModifier modify() {
        return new RetryStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RetryStatementNodeModifier {
        private final RetryStatementNode oldNode;
        private Token retryKeyword;
        private TypeParameterNode typeParameter;
        private ParenthesizedArgList arguments;
        private StatementNode retryBody;

        public RetryStatementNodeModifier(RetryStatementNode oldNode) {
            this.oldNode = oldNode;
            this.retryKeyword = oldNode.retryKeyword();
            this.typeParameter = oldNode.typeParameter().orElse(null);
            this.arguments = oldNode.arguments().orElse(null);
            this.retryBody = oldNode.retryBody();
        }

        public RetryStatementNodeModifier withRetryKeyword(
                Token retryKeyword) {
            Objects.requireNonNull(retryKeyword, "retryKeyword must not be null");
            this.retryKeyword = retryKeyword;
            return this;
        }

        public RetryStatementNodeModifier withTypeParameter(
                TypeParameterNode typeParameter) {
            Objects.requireNonNull(typeParameter, "typeParameter must not be null");
            this.typeParameter = typeParameter;
            return this;
        }

        public RetryStatementNodeModifier withArguments(
                ParenthesizedArgList arguments) {
            Objects.requireNonNull(arguments, "arguments must not be null");
            this.arguments = arguments;
            return this;
        }

        public RetryStatementNodeModifier withRetryBody(
                StatementNode retryBody) {
            Objects.requireNonNull(retryBody, "retryBody must not be null");
            this.retryBody = retryBody;
            return this;
        }

        public RetryStatementNode apply() {
            return oldNode.modify(
                    retryKeyword,
                    typeParameter,
                    arguments,
                    retryBody);
        }
    }
}
