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
public class ErrorTypeParamsNode extends NonTerminalNode {

    public ErrorTypeParamsNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ltToken() {
        return childInBucket(0);
    }

    public Node parameter() {
        return childInBucket(1);
    }

    public Token gtToken() {
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
                "ltToken",
                "parameter",
                "gtToken"};
    }

    public ErrorTypeParamsNode modify(
            Token ltToken,
            Node parameter,
            Token gtToken) {
        if (checkForReferenceEquality(
                ltToken,
                parameter,
                gtToken)) {
            return this;
        }

        return NodeFactory.createErrorTypeParamsNode(
                ltToken,
                parameter,
                gtToken);
    }

    public ErrorTypeParamsNodeModifier modify() {
        return new ErrorTypeParamsNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ErrorTypeParamsNodeModifier {
        private final ErrorTypeParamsNode oldNode;
        private Token ltToken;
        private Node parameter;
        private Token gtToken;

        public ErrorTypeParamsNodeModifier(ErrorTypeParamsNode oldNode) {
            this.oldNode = oldNode;
            this.ltToken = oldNode.ltToken();
            this.parameter = oldNode.parameter();
            this.gtToken = oldNode.gtToken();
        }

        public ErrorTypeParamsNodeModifier withLtToken(
                Token ltToken) {
            Objects.requireNonNull(ltToken, "ltToken must not be null");
            this.ltToken = ltToken;
            return this;
        }

        public ErrorTypeParamsNodeModifier withParameter(
                Node parameter) {
            Objects.requireNonNull(parameter, "parameter must not be null");
            this.parameter = parameter;
            return this;
        }

        public ErrorTypeParamsNodeModifier withGtToken(
                Token gtToken) {
            Objects.requireNonNull(gtToken, "gtToken must not be null");
            this.gtToken = gtToken;
            return this;
        }

        public ErrorTypeParamsNode apply() {
            return oldNode.modify(
                    ltToken,
                    parameter,
                    gtToken);
        }
    }
}
