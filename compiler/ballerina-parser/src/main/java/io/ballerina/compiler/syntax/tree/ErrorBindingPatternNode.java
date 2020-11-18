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
public class ErrorBindingPatternNode extends BindingPatternNode {

    public ErrorBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token errorKeyword() {
        return childInBucket(0);
    }

    public Optional<Node> typeReference() {
        return optionalChildInBucket(1);
    }

    public Token openParenthesis() {
        return childInBucket(2);
    }

    public SeparatedNodeList<BindingPatternNode> argListBindingPatterns() {
        return new SeparatedNodeList<>(childInBucket(3));
    }

    public Token closeParenthesis() {
        return childInBucket(4);
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
                "errorKeyword",
                "typeReference",
                "openParenthesis",
                "argListBindingPatterns",
                "closeParenthesis"};
    }

    public ErrorBindingPatternNode modify(
            Token errorKeyword,
            Node typeReference,
            Token openParenthesis,
            SeparatedNodeList<BindingPatternNode> argListBindingPatterns,
            Token closeParenthesis) {
        if (checkForReferenceEquality(
                errorKeyword,
                typeReference,
                openParenthesis,
                argListBindingPatterns.underlyingListNode(),
                closeParenthesis)) {
            return this;
        }

        return NodeFactory.createErrorBindingPatternNode(
                errorKeyword,
                typeReference,
                openParenthesis,
                argListBindingPatterns,
                closeParenthesis);
    }

    public ErrorBindingPatternNodeModifier modify() {
        return new ErrorBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ErrorBindingPatternNodeModifier {
        private final ErrorBindingPatternNode oldNode;
        private Token errorKeyword;
        private Node typeReference;
        private Token openParenthesis;
        private SeparatedNodeList<BindingPatternNode> argListBindingPatterns;
        private Token closeParenthesis;

        public ErrorBindingPatternNodeModifier(ErrorBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.errorKeyword = oldNode.errorKeyword();
            this.typeReference = oldNode.typeReference().orElse(null);
            this.openParenthesis = oldNode.openParenthesis();
            this.argListBindingPatterns = oldNode.argListBindingPatterns();
            this.closeParenthesis = oldNode.closeParenthesis();
        }

        public ErrorBindingPatternNodeModifier withErrorKeyword(
                Token errorKeyword) {
            Objects.requireNonNull(errorKeyword, "errorKeyword must not be null");
            this.errorKeyword = errorKeyword;
            return this;
        }

        public ErrorBindingPatternNodeModifier withTypeReference(
                Node typeReference) {
            Objects.requireNonNull(typeReference, "typeReference must not be null");
            this.typeReference = typeReference;
            return this;
        }

        public ErrorBindingPatternNodeModifier withOpenParenthesis(
                Token openParenthesis) {
            Objects.requireNonNull(openParenthesis, "openParenthesis must not be null");
            this.openParenthesis = openParenthesis;
            return this;
        }

        public ErrorBindingPatternNodeModifier withArgListBindingPatterns(
                SeparatedNodeList<BindingPatternNode> argListBindingPatterns) {
            Objects.requireNonNull(argListBindingPatterns, "argListBindingPatterns must not be null");
            this.argListBindingPatterns = argListBindingPatterns;
            return this;
        }

        public ErrorBindingPatternNodeModifier withCloseParenthesis(
                Token closeParenthesis) {
            Objects.requireNonNull(closeParenthesis, "closeParenthesis must not be null");
            this.closeParenthesis = closeParenthesis;
            return this;
        }

        public ErrorBindingPatternNode apply() {
            return oldNode.modify(
                    errorKeyword,
                    typeReference,
                    openParenthesis,
                    argListBindingPatterns,
                    closeParenthesis);
        }
    }
}
