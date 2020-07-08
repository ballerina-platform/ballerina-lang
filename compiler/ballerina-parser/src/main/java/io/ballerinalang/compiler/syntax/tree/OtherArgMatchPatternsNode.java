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
public class OtherArgMatchPatternsNode extends NonTerminalNode {

    public OtherArgMatchPatternsNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SeparatedNodeList<NamedArgMatchPatternNode> namedArgMatchPatternsNode() {
        return new SeparatedNodeList<>(childInBucket(0));
    }

    public Optional<RestMatchPatternNode> restMatchPattern() {
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
                "namedArgMatchPatternsNode",
                "restMatchPattern"};
    }

    public OtherArgMatchPatternsNode modify(
            SeparatedNodeList<NamedArgMatchPatternNode> namedArgMatchPatternsNode,
            RestMatchPatternNode restMatchPattern) {
        if (checkForReferenceEquality(
                namedArgMatchPatternsNode.underlyingListNode(),
                restMatchPattern)) {
            return this;
        }

        return NodeFactory.createOtherArgMatchPatternsNode(
                namedArgMatchPatternsNode,
                restMatchPattern);
    }

    public OtherArgMatchPatternsNodeModifier modify() {
        return new OtherArgMatchPatternsNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OtherArgMatchPatternsNodeModifier {
        private final OtherArgMatchPatternsNode oldNode;
        private SeparatedNodeList<NamedArgMatchPatternNode> namedArgMatchPatternsNode;
        private RestMatchPatternNode restMatchPattern;

        public OtherArgMatchPatternsNodeModifier(OtherArgMatchPatternsNode oldNode) {
            this.oldNode = oldNode;
            this.namedArgMatchPatternsNode = oldNode.namedArgMatchPatternsNode();
            this.restMatchPattern = oldNode.restMatchPattern().orElse(null);
        }

        public OtherArgMatchPatternsNodeModifier withNamedArgMatchPatternsNode(
                SeparatedNodeList<NamedArgMatchPatternNode> namedArgMatchPatternsNode) {
            Objects.requireNonNull(namedArgMatchPatternsNode, "namedArgMatchPatternsNode must not be null");
            this.namedArgMatchPatternsNode = namedArgMatchPatternsNode;
            return this;
        }

        public OtherArgMatchPatternsNodeModifier withRestMatchPattern(
                RestMatchPatternNode restMatchPattern) {
            Objects.requireNonNull(restMatchPattern, "restMatchPattern must not be null");
            this.restMatchPattern = restMatchPattern;
            return this;
        }

        public OtherArgMatchPatternsNode apply() {
            return oldNode.modify(
                    namedArgMatchPatternsNode,
                    restMatchPattern);
        }
    }
}
