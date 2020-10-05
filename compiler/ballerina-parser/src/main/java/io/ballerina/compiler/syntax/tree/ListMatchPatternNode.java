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
public class ListMatchPatternNode extends NonTerminalNode {

    public ListMatchPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public SeparatedNodeList<Node> matchPatterns() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Optional<RestMatchPatternNode> restMatchPattern() {
        return optionalChildInBucket(2);
    }

    public Token closeBracket() {
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
                "openBracket",
                "matchPatterns",
                "restMatchPattern",
                "closeBracket"};
    }

    public ListMatchPatternNode modify(
            Token openBracket,
            SeparatedNodeList<Node> matchPatterns,
            RestMatchPatternNode restMatchPattern,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                matchPatterns.underlyingListNode(),
                restMatchPattern,
                closeBracket)) {
            return this;
        }

        return NodeFactory.createListMatchPatternNode(
                openBracket,
                matchPatterns,
                restMatchPattern,
                closeBracket);
    }

    public ListMatchPatternNodeModifier modify() {
        return new ListMatchPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ListMatchPatternNodeModifier {
        private final ListMatchPatternNode oldNode;
        private Token openBracket;
        private SeparatedNodeList<Node> matchPatterns;
        private RestMatchPatternNode restMatchPattern;
        private Token closeBracket;

        public ListMatchPatternNodeModifier(ListMatchPatternNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.matchPatterns = oldNode.matchPatterns();
            this.restMatchPattern = oldNode.restMatchPattern().orElse(null);
            this.closeBracket = oldNode.closeBracket();
        }

        public ListMatchPatternNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public ListMatchPatternNodeModifier withMatchPatterns(
                SeparatedNodeList<Node> matchPatterns) {
            Objects.requireNonNull(matchPatterns, "matchPatterns must not be null");
            this.matchPatterns = matchPatterns;
            return this;
        }

        public ListMatchPatternNodeModifier withRestMatchPattern(
                RestMatchPatternNode restMatchPattern) {
            Objects.requireNonNull(restMatchPattern, "restMatchPattern must not be null");
            this.restMatchPattern = restMatchPattern;
            return this;
        }

        public ListMatchPatternNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public ListMatchPatternNode apply() {
            return oldNode.modify(
                    openBracket,
                    matchPatterns,
                    restMatchPattern,
                    closeBracket);
        }
    }
}
