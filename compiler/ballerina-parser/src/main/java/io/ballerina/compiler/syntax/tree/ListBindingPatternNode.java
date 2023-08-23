/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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

    public Token closeBracket() {
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
                "openBracket",
                "bindingPatterns",
                "closeBracket"};
    }

    public ListBindingPatternNode modify(
            Token openBracket,
            SeparatedNodeList<BindingPatternNode> bindingPatterns,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                bindingPatterns.underlyingListNode(),
                closeBracket)) {
            return this;
        }

        return NodeFactory.createListBindingPatternNode(
                openBracket,
                bindingPatterns,
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
        private Token closeBracket;

        public ListBindingPatternNodeModifier(ListBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.bindingPatterns = oldNode.bindingPatterns();
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
                    closeBracket);
        }
    }
}
