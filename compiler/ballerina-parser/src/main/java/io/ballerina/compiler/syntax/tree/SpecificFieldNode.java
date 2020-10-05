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
public class SpecificFieldNode extends MappingFieldNode {

    public SpecificFieldNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> readonlyKeyword() {
        return optionalChildInBucket(0);
    }

    public Node fieldName() {
        return childInBucket(1);
    }

    public Optional<Token> colon() {
        return optionalChildInBucket(2);
    }

    public Optional<ExpressionNode> valueExpr() {
        return optionalChildInBucket(3);
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
                "readonlyKeyword",
                "fieldName",
                "colon",
                "valueExpr"};
    }

    public SpecificFieldNode modify(
            Token readonlyKeyword,
            Node fieldName,
            Token colon,
            ExpressionNode valueExpr) {
        if (checkForReferenceEquality(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr)) {
            return this;
        }

        return NodeFactory.createSpecificFieldNode(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr);
    }

    public SpecificFieldNodeModifier modify() {
        return new SpecificFieldNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class SpecificFieldNodeModifier {
        private final SpecificFieldNode oldNode;
        private Token readonlyKeyword;
        private Node fieldName;
        private Token colon;
        private ExpressionNode valueExpr;

        public SpecificFieldNodeModifier(SpecificFieldNode oldNode) {
            this.oldNode = oldNode;
            this.readonlyKeyword = oldNode.readonlyKeyword().orElse(null);
            this.fieldName = oldNode.fieldName();
            this.colon = oldNode.colon().orElse(null);
            this.valueExpr = oldNode.valueExpr().orElse(null);
        }

        public SpecificFieldNodeModifier withReadonlyKeyword(
                Token readonlyKeyword) {
            Objects.requireNonNull(readonlyKeyword, "readonlyKeyword must not be null");
            this.readonlyKeyword = readonlyKeyword;
            return this;
        }

        public SpecificFieldNodeModifier withFieldName(
                Node fieldName) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            this.fieldName = fieldName;
            return this;
        }

        public SpecificFieldNodeModifier withColon(
                Token colon) {
            Objects.requireNonNull(colon, "colon must not be null");
            this.colon = colon;
            return this;
        }

        public SpecificFieldNodeModifier withValueExpr(
                ExpressionNode valueExpr) {
            Objects.requireNonNull(valueExpr, "valueExpr must not be null");
            this.valueExpr = valueExpr;
            return this;
        }

        public SpecificFieldNode apply() {
            return oldNode.modify(
                    readonlyKeyword,
                    fieldName,
                    colon,
                    valueExpr);
        }
    }
}
