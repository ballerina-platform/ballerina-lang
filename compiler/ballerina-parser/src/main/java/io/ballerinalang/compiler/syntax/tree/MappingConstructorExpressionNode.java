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
public class MappingConstructorExpressionNode extends ExpressionNode {

    public MappingConstructorExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBrace() {
        return childInBucket(0);
    }

    public SeparatedNodeList<MappingFieldNode> fields() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token closeBrace() {
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
                "openBrace",
                "fields",
                "closeBrace"};
    }

    public MappingConstructorExpressionNode modify(
            Token openBrace,
            SeparatedNodeList<MappingFieldNode> fields,
            Token closeBrace) {
        if (checkForReferenceEquality(
                openBrace,
                fields.underlyingListNode(),
                closeBrace)) {
            return this;
        }

        return NodeFactory.createMappingConstructorExpressionNode(
                openBrace,
                fields,
                closeBrace);
    }

    public MappingConstructorExpressionNodeModifier modify() {
        return new MappingConstructorExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MappingConstructorExpressionNodeModifier {
        private final MappingConstructorExpressionNode oldNode;
        private Token openBrace;
        private SeparatedNodeList<MappingFieldNode> fields;
        private Token closeBrace;

        public MappingConstructorExpressionNodeModifier(MappingConstructorExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.openBrace = oldNode.openBrace();
            this.fields = oldNode.fields();
            this.closeBrace = oldNode.closeBrace();
        }

        public MappingConstructorExpressionNodeModifier withOpenBrace(
                Token openBrace) {
            Objects.requireNonNull(openBrace, "openBrace must not be null");
            this.openBrace = openBrace;
            return this;
        }

        public MappingConstructorExpressionNodeModifier withFields(
                SeparatedNodeList<MappingFieldNode> fields) {
            Objects.requireNonNull(fields, "fields must not be null");
            this.fields = fields;
            return this;
        }

        public MappingConstructorExpressionNodeModifier withCloseBrace(
                Token closeBrace) {
            Objects.requireNonNull(closeBrace, "closeBrace must not be null");
            this.closeBrace = closeBrace;
            return this;
        }

        public MappingConstructorExpressionNode apply() {
            return oldNode.modify(
                    openBrace,
                    fields,
                    closeBrace);
        }
    }
}
