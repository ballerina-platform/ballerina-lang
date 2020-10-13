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
public class ExternalFunctionBodyNode extends FunctionBodyNode {

    public ExternalFunctionBodyNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token equalsToken() {
        return childInBucket(0);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(1));
    }

    public Token externalKeyword() {
        return childInBucket(2);
    }

    public Token semicolonToken() {
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
                "equalsToken",
                "annotations",
                "externalKeyword",
                "semicolonToken"};
    }

    public ExternalFunctionBodyNode modify(
            Token equalsToken,
            NodeList<AnnotationNode> annotations,
            Token externalKeyword,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                equalsToken,
                annotations.underlyingListNode(),
                externalKeyword,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createExternalFunctionBodyNode(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken);
    }

    public ExternalFunctionBodyNodeModifier modify() {
        return new ExternalFunctionBodyNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ExternalFunctionBodyNodeModifier {
        private final ExternalFunctionBodyNode oldNode;
        private Token equalsToken;
        private NodeList<AnnotationNode> annotations;
        private Token externalKeyword;
        private Token semicolonToken;

        public ExternalFunctionBodyNodeModifier(ExternalFunctionBodyNode oldNode) {
            this.oldNode = oldNode;
            this.equalsToken = oldNode.equalsToken();
            this.annotations = oldNode.annotations();
            this.externalKeyword = oldNode.externalKeyword();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ExternalFunctionBodyNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public ExternalFunctionBodyNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public ExternalFunctionBodyNodeModifier withExternalKeyword(
                Token externalKeyword) {
            Objects.requireNonNull(externalKeyword, "externalKeyword must not be null");
            this.externalKeyword = externalKeyword;
            return this;
        }

        public ExternalFunctionBodyNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ExternalFunctionBodyNode apply() {
            return oldNode.modify(
                    equalsToken,
                    annotations,
                    externalKeyword,
                    semicolonToken);
        }
    }
}
