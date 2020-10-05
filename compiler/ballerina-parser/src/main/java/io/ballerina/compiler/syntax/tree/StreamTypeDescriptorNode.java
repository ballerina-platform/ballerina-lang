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
public class StreamTypeDescriptorNode extends TypeDescriptorNode {

    public StreamTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token streamKeywordToken() {
        return childInBucket(0);
    }

    public Optional<Node> streamTypeParamsNode() {
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
                "streamKeywordToken",
                "streamTypeParamsNode"};
    }

    public StreamTypeDescriptorNode modify(
            Token streamKeywordToken,
            Node streamTypeParamsNode) {
        if (checkForReferenceEquality(
                streamKeywordToken,
                streamTypeParamsNode)) {
            return this;
        }

        return NodeFactory.createStreamTypeDescriptorNode(
                streamKeywordToken,
                streamTypeParamsNode);
    }

    public StreamTypeDescriptorNodeModifier modify() {
        return new StreamTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class StreamTypeDescriptorNodeModifier {
        private final StreamTypeDescriptorNode oldNode;
        private Token streamKeywordToken;
        private Node streamTypeParamsNode;

        public StreamTypeDescriptorNodeModifier(StreamTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.streamKeywordToken = oldNode.streamKeywordToken();
            this.streamTypeParamsNode = oldNode.streamTypeParamsNode().orElse(null);
        }

        public StreamTypeDescriptorNodeModifier withStreamKeywordToken(
                Token streamKeywordToken) {
            Objects.requireNonNull(streamKeywordToken, "streamKeywordToken must not be null");
            this.streamKeywordToken = streamKeywordToken;
            return this;
        }

        public StreamTypeDescriptorNodeModifier withStreamTypeParamsNode(
                Node streamTypeParamsNode) {
            Objects.requireNonNull(streamTypeParamsNode, "streamTypeParamsNode must not be null");
            this.streamTypeParamsNode = streamTypeParamsNode;
            return this;
        }

        public StreamTypeDescriptorNode apply() {
            return oldNode.modify(
                    streamKeywordToken,
                    streamTypeParamsNode);
        }
    }
}
