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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class FunctionalBindingPatternNode extends BindingPatternNode {

    public FunctionalBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node typeReference() {
        return childInBucket(0);
    }

    public Token openParenthesis() {
        return childInBucket(1);
    }

    public SeparatedNodeList<BindingPatternNode> argListBindingPatterns() {
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
                "typeReference",
                "openParenthesis",
                "argListBindingPatterns",
                "closeParenthesis"};
    }

    public FunctionalBindingPatternNode modify(
            Node typeReference,
            Token openParenthesis,
            SeparatedNodeList<BindingPatternNode> argListBindingPatterns,
            Token closeParenthesis) {
        if (checkForReferenceEquality(
                typeReference,
                openParenthesis,
                argListBindingPatterns.underlyingListNode(),
                closeParenthesis)) {
            return this;
        }

        return NodeFactory.createFunctionalBindingPatternNode(
                typeReference,
                openParenthesis,
                argListBindingPatterns,
                closeParenthesis);
    }

    public FunctionalBindingPatternNodeModifier modify() {
        return new FunctionalBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FunctionalBindingPatternNodeModifier {
        private final FunctionalBindingPatternNode oldNode;
        private Node typeReference;
        private Token openParenthesis;
        private SeparatedNodeList<BindingPatternNode> argListBindingPatterns;
        private Token closeParenthesis;

        public FunctionalBindingPatternNodeModifier(FunctionalBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.typeReference = oldNode.typeReference();
            this.openParenthesis = oldNode.openParenthesis();
            this.argListBindingPatterns = oldNode.argListBindingPatterns();
            this.closeParenthesis = oldNode.closeParenthesis();
        }

        public FunctionalBindingPatternNodeModifier withTypeReference(
                Node typeReference) {
            Objects.requireNonNull(typeReference, "typeReference must not be null");
            this.typeReference = typeReference;
            return this;
        }

        public FunctionalBindingPatternNodeModifier withOpenParenthesis(
                Token openParenthesis) {
            Objects.requireNonNull(openParenthesis, "openParenthesis must not be null");
            this.openParenthesis = openParenthesis;
            return this;
        }

        public FunctionalBindingPatternNodeModifier withArgListBindingPatterns(
                SeparatedNodeList<BindingPatternNode> argListBindingPatterns) {
            Objects.requireNonNull(argListBindingPatterns, "argListBindingPatterns must not be null");
            this.argListBindingPatterns = argListBindingPatterns;
            return this;
        }

        public FunctionalBindingPatternNodeModifier withCloseParenthesis(
                Token closeParenthesis) {
            Objects.requireNonNull(closeParenthesis, "closeParenthesis must not be null");
            this.closeParenthesis = closeParenthesis;
            return this;
        }

        public FunctionalBindingPatternNode apply() {
            return oldNode.modify(
                    typeReference,
                    openParenthesis,
                    argListBindingPatterns,
                    closeParenthesis);
        }
    }
}
