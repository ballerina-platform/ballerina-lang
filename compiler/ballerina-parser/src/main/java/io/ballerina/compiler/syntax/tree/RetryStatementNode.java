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

    public Optional<OnFailClauseNode> onFailClause() {
        return optionalChildInBucket(4);
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
                "retryBody",
                "onFailClause"};
    }

    public RetryStatementNode modify(
            Token retryKeyword,
            TypeParameterNode typeParameter,
            ParenthesizedArgList arguments,
            StatementNode retryBody,
            OnFailClauseNode onFailClause) {
        if (checkForReferenceEquality(
                retryKeyword,
                typeParameter,
                arguments,
                retryBody,
                onFailClause)) {
            return this;
        }

        return NodeFactory.createRetryStatementNode(
                retryKeyword,
                typeParameter,
                arguments,
                retryBody,
                onFailClause);
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
        private OnFailClauseNode onFailClause;

        public RetryStatementNodeModifier(RetryStatementNode oldNode) {
            this.oldNode = oldNode;
            this.retryKeyword = oldNode.retryKeyword();
            this.typeParameter = oldNode.typeParameter().orElse(null);
            this.arguments = oldNode.arguments().orElse(null);
            this.retryBody = oldNode.retryBody();
            this.onFailClause = oldNode.onFailClause().orElse(null);
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

        public RetryStatementNodeModifier withOnFailClause(
                OnFailClauseNode onFailClause) {
            Objects.requireNonNull(onFailClause, "onFailClause must not be null");
            this.onFailClause = onFailClause;
            return this;
        }

        public RetryStatementNode apply() {
            return oldNode.modify(
                    retryKeyword,
                    typeParameter,
                    arguments,
                    retryBody,
                    onFailClause);
        }
    }
}
