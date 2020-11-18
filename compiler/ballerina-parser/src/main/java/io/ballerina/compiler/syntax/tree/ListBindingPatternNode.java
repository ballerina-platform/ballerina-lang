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
public class ListBindingPatternNode extends BindingPatternNode {

    public ListBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public SeparatedNodeList<BindingPatternNode> bindingPatterns() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Optional<RestBindingPatternNode> restBindingPattern() {
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
                "bindingPatterns",
                "restBindingPattern",
                "closeBracket"};
    }

    public ListBindingPatternNode modify(
            Token openBracket,
            SeparatedNodeList<BindingPatternNode> bindingPatterns,
            RestBindingPatternNode restBindingPattern,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                bindingPatterns.underlyingListNode(),
                restBindingPattern,
                closeBracket)) {
            return this;
        }

        return NodeFactory.createListBindingPatternNode(
                openBracket,
                bindingPatterns,
                restBindingPattern,
                closeBracket);
    }

    public ListBindingPatternNodeModifier modify() {
        return new ListBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ListBindingPatternNodeModifier {
        private final ListBindingPatternNode oldNode;
        private Token openBracket;
        private SeparatedNodeList<BindingPatternNode> bindingPatterns;
        private RestBindingPatternNode restBindingPattern;
        private Token closeBracket;

        public ListBindingPatternNodeModifier(ListBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.bindingPatterns = oldNode.bindingPatterns();
            this.restBindingPattern = oldNode.restBindingPattern().orElse(null);
            this.closeBracket = oldNode.closeBracket();
        }

        public ListBindingPatternNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public ListBindingPatternNodeModifier withBindingPatterns(
                SeparatedNodeList<BindingPatternNode> bindingPatterns) {
            Objects.requireNonNull(bindingPatterns, "bindingPatterns must not be null");
            this.bindingPatterns = bindingPatterns;
            return this;
        }

        public ListBindingPatternNodeModifier withRestBindingPattern(
                RestBindingPatternNode restBindingPattern) {
            Objects.requireNonNull(restBindingPattern, "restBindingPattern must not be null");
            this.restBindingPattern = restBindingPattern;
            return this;
        }

        public ListBindingPatternNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public ListBindingPatternNode apply() {
            return oldNode.modify(
                    openBracket,
                    bindingPatterns,
                    restBindingPattern,
                    closeBracket);
        }
    }
}
