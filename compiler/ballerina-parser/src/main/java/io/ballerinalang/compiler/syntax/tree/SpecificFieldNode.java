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

    public Token fieldName() {
        return childInBucket(1);
    }

    public Token colon() {
        return childInBucket(2);
    }

    public ExpressionNode valueExpr() {
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
                "readonlyKeyword",
                "fieldName",
                "colon",
                "valueExpr"};
    }

    public SpecificFieldNode modify(
            Token readonlyKeyword,
            Token fieldName,
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
        private Token fieldName;
        private Token colon;
        private ExpressionNode valueExpr;

        public SpecificFieldNodeModifier(SpecificFieldNode oldNode) {
            this.oldNode = oldNode;
            this.readonlyKeyword = oldNode.readonlyKeyword().orElse(null);
            this.fieldName = oldNode.fieldName();
            this.colon = oldNode.colon();
            this.valueExpr = oldNode.valueExpr();
        }

        public SpecificFieldNodeModifier withReadonlyKeyword(
                Token readonlyKeyword) {
            Objects.requireNonNull(readonlyKeyword, "readonlyKeyword must not be null");
            this.readonlyKeyword = readonlyKeyword;
            return this;
        }

        public SpecificFieldNodeModifier withFieldName(
                Token fieldName) {
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
