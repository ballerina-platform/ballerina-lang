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
public class FieldMatchPatternNode extends NonTerminalNode {

    public FieldMatchPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SimpleNameReferenceNode fieldNameNode() {
        return childInBucket(0);
    }

    public Token colonToken() {
        return childInBucket(1);
    }

    public Node matchPattern() {
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
                "fieldNameNode",
                "colonToken",
                "matchPattern"};
    }

    public FieldMatchPatternNode modify(
            SimpleNameReferenceNode fieldNameNode,
            Token colonToken,
            Node matchPattern) {
        if (checkForReferenceEquality(
                fieldNameNode,
                colonToken,
                matchPattern)) {
            return this;
        }

        return NodeFactory.createFieldMatchPatternNode(
                fieldNameNode,
                colonToken,
                matchPattern);
    }

    public FieldMatchPatternNodeModifier modify() {
        return new FieldMatchPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FieldMatchPatternNodeModifier {
        private final FieldMatchPatternNode oldNode;
        private SimpleNameReferenceNode fieldNameNode;
        private Token colonToken;
        private Node matchPattern;

        public FieldMatchPatternNodeModifier(FieldMatchPatternNode oldNode) {
            this.oldNode = oldNode;
            this.fieldNameNode = oldNode.fieldNameNode();
            this.colonToken = oldNode.colonToken();
            this.matchPattern = oldNode.matchPattern();
        }

        public FieldMatchPatternNodeModifier withFieldNameNode(
                SimpleNameReferenceNode fieldNameNode) {
            Objects.requireNonNull(fieldNameNode, "fieldNameNode must not be null");
            this.fieldNameNode = fieldNameNode;
            return this;
        }

        public FieldMatchPatternNodeModifier withColonToken(
                Token colonToken) {
            Objects.requireNonNull(colonToken, "colonToken must not be null");
            this.colonToken = colonToken;
            return this;
        }

        public FieldMatchPatternNodeModifier withMatchPattern(
                Node matchPattern) {
            Objects.requireNonNull(matchPattern, "matchPattern must not be null");
            this.matchPattern = matchPattern;
            return this;
        }

        public FieldMatchPatternNode apply() {
            return oldNode.modify(
                    fieldNameNode,
                    colonToken,
                    matchPattern);
        }
    }
}
