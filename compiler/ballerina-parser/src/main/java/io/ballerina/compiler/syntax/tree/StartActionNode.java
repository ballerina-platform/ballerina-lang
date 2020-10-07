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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class StartActionNode extends ExpressionNode {

    public StartActionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public Token startKeyword() {
        return childInBucket(1);
    }

    public ExpressionNode expression() {
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
                "annotations",
                "startKeyword",
                "expression"};
    }

    public StartActionNode modify(
            NodeList<AnnotationNode> annotations,
            Token startKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                startKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createStartActionNode(
                annotations,
                startKeyword,
                expression);
    }

    public StartActionNodeModifier modify() {
        return new StartActionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class StartActionNodeModifier {
        private final StartActionNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private Token startKeyword;
        private ExpressionNode expression;

        public StartActionNodeModifier(StartActionNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.startKeyword = oldNode.startKeyword();
            this.expression = oldNode.expression();
        }

        public StartActionNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public StartActionNodeModifier withStartKeyword(
                Token startKeyword) {
            Objects.requireNonNull(startKeyword, "startKeyword must not be null");
            this.startKeyword = startKeyword;
            return this;
        }

        public StartActionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public StartActionNode apply() {
            return oldNode.modify(
                    annotations,
                    startKeyword,
                    expression);
        }
    }
}
