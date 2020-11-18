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
public class AnnotationAttachPointNode extends NonTerminalNode {

    public AnnotationAttachPointNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> sourceKeyword() {
        return optionalChildInBucket(0);
    }

    public Token firstIdent() {
        return childInBucket(1);
    }

    public Optional<Token> secondIdent() {
        return optionalChildInBucket(2);
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
                "sourceKeyword",
                "firstIdent",
                "secondIdent"};
    }

    public AnnotationAttachPointNode modify(
            Token sourceKeyword,
            Token firstIdent,
            Token secondIdent) {
        if (checkForReferenceEquality(
                sourceKeyword,
                firstIdent,
                secondIdent)) {
            return this;
        }

        return NodeFactory.createAnnotationAttachPointNode(
                sourceKeyword,
                firstIdent,
                secondIdent);
    }

    public AnnotationAttachPointNodeModifier modify() {
        return new AnnotationAttachPointNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class AnnotationAttachPointNodeModifier {
        private final AnnotationAttachPointNode oldNode;
        private Token sourceKeyword;
        private Token firstIdent;
        private Token secondIdent;

        public AnnotationAttachPointNodeModifier(AnnotationAttachPointNode oldNode) {
            this.oldNode = oldNode;
            this.sourceKeyword = oldNode.sourceKeyword().orElse(null);
            this.firstIdent = oldNode.firstIdent();
            this.secondIdent = oldNode.secondIdent().orElse(null);
        }

        public AnnotationAttachPointNodeModifier withSourceKeyword(
                Token sourceKeyword) {
            Objects.requireNonNull(sourceKeyword, "sourceKeyword must not be null");
            this.sourceKeyword = sourceKeyword;
            return this;
        }

        public AnnotationAttachPointNodeModifier withFirstIdent(
                Token firstIdent) {
            Objects.requireNonNull(firstIdent, "firstIdent must not be null");
            this.firstIdent = firstIdent;
            return this;
        }

        public AnnotationAttachPointNodeModifier withSecondIdent(
                Token secondIdent) {
            Objects.requireNonNull(secondIdent, "secondIdent must not be null");
            this.secondIdent = secondIdent;
            return this;
        }

        public AnnotationAttachPointNode apply() {
            return oldNode.modify(
                    sourceKeyword,
                    firstIdent,
                    secondIdent);
        }
    }
}
