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
public class ExplicitAnonymousFunctionExpressionNode extends AnonymousFunctionExpressionNode {

    public ExplicitAnonymousFunctionExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public Token functionKeyword() {
        return childInBucket(1);
    }

    public FunctionSignatureNode functionSignature() {
        return childInBucket(2);
    }

    public FunctionBodyNode functionBody() {
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
                "annotations",
                "functionKeyword",
                "functionSignature",
                "functionBody"};
    }

    public ExplicitAnonymousFunctionExpressionNode modify(
            NodeList<AnnotationNode> annotations,
            Token functionKeyword,
            FunctionSignatureNode functionSignature,
            FunctionBodyNode functionBody) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                functionKeyword,
                functionSignature,
                functionBody)) {
            return this;
        }

        return NodeFactory.createExplicitAnonymousFunctionExpressionNode(
                annotations,
                functionKeyword,
                functionSignature,
                functionBody);
    }

    public ExplicitAnonymousFunctionExpressionNodeModifier modify() {
        return new ExplicitAnonymousFunctionExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ExplicitAnonymousFunctionExpressionNodeModifier {
        private final ExplicitAnonymousFunctionExpressionNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private Token functionKeyword;
        private FunctionSignatureNode functionSignature;
        private FunctionBodyNode functionBody;

        public ExplicitAnonymousFunctionExpressionNodeModifier(ExplicitAnonymousFunctionExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.functionKeyword = oldNode.functionKeyword();
            this.functionSignature = oldNode.functionSignature();
            this.functionBody = oldNode.functionBody();
        }

        public ExplicitAnonymousFunctionExpressionNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public ExplicitAnonymousFunctionExpressionNodeModifier withFunctionKeyword(
                Token functionKeyword) {
            Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
            this.functionKeyword = functionKeyword;
            return this;
        }

        public ExplicitAnonymousFunctionExpressionNodeModifier withFunctionSignature(
                FunctionSignatureNode functionSignature) {
            Objects.requireNonNull(functionSignature, "functionSignature must not be null");
            this.functionSignature = functionSignature;
            return this;
        }

        public ExplicitAnonymousFunctionExpressionNodeModifier withFunctionBody(
                FunctionBodyNode functionBody) {
            Objects.requireNonNull(functionBody, "functionBody must not be null");
            this.functionBody = functionBody;
            return this;
        }

        public ExplicitAnonymousFunctionExpressionNode apply() {
            return oldNode.modify(
                    annotations,
                    functionKeyword,
                    functionSignature,
                    functionBody);
        }
    }
}
