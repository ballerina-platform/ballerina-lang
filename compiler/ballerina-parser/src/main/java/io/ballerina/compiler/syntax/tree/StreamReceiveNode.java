/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.9.0
 */
public class StreamReceiveNode extends NonTerminalNode {

    public StreamReceiveNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token streamKeyword() {
        return childInBucket(0);
    }

    public Token openParenthesis() {
        return childInBucket(1);
    }

    public SeparatedNodeList<SimpleNameReferenceNode> workers() {
        return new SeparatedNodeList<>(childInBucket(2));
    }

    public Token closeParenthesis() {
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
                "streamKeyword",
                "openParenthesis",
                "workers",
                "closeParenthesis"};
    }

    public StreamReceiveNode modify(
            Token streamKeyword,
            Token openParenthesis,
            SeparatedNodeList<SimpleNameReferenceNode> workers,
            Token closeParenthesis) {
        if (checkForReferenceEquality(
                streamKeyword,
                openParenthesis,
                workers.underlyingListNode(),
                closeParenthesis)) {
            return this;
        }

        return NodeFactory.createStreamReceiveNode(
                streamKeyword,
                openParenthesis,
                workers,
                closeParenthesis);
    }

    public StreamReceiveNodeModifier modify() {
        return new StreamReceiveNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.9.0
     */
    public static class StreamReceiveNodeModifier {
        private final StreamReceiveNode oldNode;
        private Token streamKeyword;
        private Token openParenthesis;
        private SeparatedNodeList<SimpleNameReferenceNode> workers;
        private Token closeParenthesis;

        public StreamReceiveNodeModifier(StreamReceiveNode oldNode) {
            this.oldNode = oldNode;
            this.streamKeyword = oldNode.streamKeyword();
            this.openParenthesis = oldNode.openParenthesis();
            this.workers = oldNode.workers();
            this.closeParenthesis = oldNode.closeParenthesis();
        }

        public StreamReceiveNodeModifier withStreamKeyword(
                Token streamKeyword) {
            Objects.requireNonNull(streamKeyword, "streamKeyword must not be null");
            this.streamKeyword = streamKeyword;
            return this;
        }

        public StreamReceiveNodeModifier withOpenParenthesis(
                Token openParenthesis) {
            Objects.requireNonNull(openParenthesis, "openParenthesis must not be null");
            this.openParenthesis = openParenthesis;
            return this;
        }

        public StreamReceiveNodeModifier withWorkers(
                SeparatedNodeList<SimpleNameReferenceNode> workers) {
            Objects.requireNonNull(workers, "workers must not be null");
            this.workers = workers;
            return this;
        }

        public StreamReceiveNodeModifier withCloseParenthesis(
                Token closeParenthesis) {
            Objects.requireNonNull(closeParenthesis, "closeParenthesis must not be null");
            this.closeParenthesis = closeParenthesis;
            return this;
        }

        public StreamReceiveNode apply() {
            return oldNode.modify(
                    streamKeyword,
                    openParenthesis,
                    workers,
                    closeParenthesis);
        }
    }
}
