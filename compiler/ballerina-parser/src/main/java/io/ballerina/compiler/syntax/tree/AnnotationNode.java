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
public class AnnotationNode extends NonTerminalNode {

    public AnnotationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token atToken() {
        return childInBucket(0);
    }

    public Node annotReference() {
        return childInBucket(1);
    }

    public Optional<MappingConstructorExpressionNode> annotValue() {
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
                "atToken",
                "annotReference",
                "annotValue"};
    }

    public AnnotationNode modify(
            Token atToken,
            Node annotReference,
            MappingConstructorExpressionNode annotValue) {
        if (checkForReferenceEquality(
                atToken,
                annotReference,
                annotValue)) {
            return this;
        }

        return NodeFactory.createAnnotationNode(
                atToken,
                annotReference,
                annotValue);
    }

    public AnnotationNodeModifier modify() {
        return new AnnotationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class AnnotationNodeModifier {
        private final AnnotationNode oldNode;
        private Token atToken;
        private Node annotReference;
        private MappingConstructorExpressionNode annotValue;

        public AnnotationNodeModifier(AnnotationNode oldNode) {
            this.oldNode = oldNode;
            this.atToken = oldNode.atToken();
            this.annotReference = oldNode.annotReference();
            this.annotValue = oldNode.annotValue().orElse(null);
        }

        public AnnotationNodeModifier withAtToken(
                Token atToken) {
            Objects.requireNonNull(atToken, "atToken must not be null");
            this.atToken = atToken;
            return this;
        }

        public AnnotationNodeModifier withAnnotReference(
                Node annotReference) {
            Objects.requireNonNull(annotReference, "annotReference must not be null");
            this.annotReference = annotReference;
            return this;
        }

        public AnnotationNodeModifier withAnnotValue(
                MappingConstructorExpressionNode annotValue) {
            Objects.requireNonNull(annotValue, "annotValue must not be null");
            this.annotValue = annotValue;
            return this;
        }

        public AnnotationNode apply() {
            return oldNode.modify(
                    atToken,
                    annotReference,
                    annotValue);
        }
    }
}
