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
public class ErrorTypeDescriptorNode extends TypeDescriptorNode {

    public ErrorTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token errorKeywordToken() {
        return childInBucket(0);
    }

    public Optional<ErrorTypeParamsNode> errorTypeParamsNode() {
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
                "errorKeywordToken",
                "errorTypeParamsNode"};
    }

    public ErrorTypeDescriptorNode modify(
            Token errorKeywordToken,
            ErrorTypeParamsNode errorTypeParamsNode) {
        if (checkForReferenceEquality(
                errorKeywordToken,
                errorTypeParamsNode)) {
            return this;
        }

        return NodeFactory.createErrorTypeDescriptorNode(
                errorKeywordToken,
                errorTypeParamsNode);
    }

    public ErrorTypeDescriptorNodeModifier modify() {
        return new ErrorTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ErrorTypeDescriptorNodeModifier {
        private final ErrorTypeDescriptorNode oldNode;
        private Token errorKeywordToken;
        private ErrorTypeParamsNode errorTypeParamsNode;

        public ErrorTypeDescriptorNodeModifier(ErrorTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.errorKeywordToken = oldNode.errorKeywordToken();
            this.errorTypeParamsNode = oldNode.errorTypeParamsNode().orElse(null);
        }

        public ErrorTypeDescriptorNodeModifier withErrorKeywordToken(
                Token errorKeywordToken) {
            Objects.requireNonNull(errorKeywordToken, "errorKeywordToken must not be null");
            this.errorKeywordToken = errorKeywordToken;
            return this;
        }

        public ErrorTypeDescriptorNodeModifier withErrorTypeParamsNode(
                ErrorTypeParamsNode errorTypeParamsNode) {
            Objects.requireNonNull(errorTypeParamsNode, "errorTypeParamsNode must not be null");
            this.errorTypeParamsNode = errorTypeParamsNode;
            return this;
        }

        public ErrorTypeDescriptorNode apply() {
            return oldNode.modify(
                    errorKeywordToken,
                    errorTypeParamsNode);
        }
    }
}
