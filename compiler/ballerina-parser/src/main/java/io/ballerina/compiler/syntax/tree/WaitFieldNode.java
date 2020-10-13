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
public class WaitFieldNode extends NonTerminalNode {

    public WaitFieldNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SimpleNameReferenceNode fieldName() {
        return childInBucket(0);
    }

    public Token colon() {
        return childInBucket(1);
    }

    public ExpressionNode waitFutureExpr() {
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
                "fieldName",
                "colon",
                "waitFutureExpr"};
    }

    public WaitFieldNode modify(
            SimpleNameReferenceNode fieldName,
            Token colon,
            ExpressionNode waitFutureExpr) {
        if (checkForReferenceEquality(
                fieldName,
                colon,
                waitFutureExpr)) {
            return this;
        }

        return NodeFactory.createWaitFieldNode(
                fieldName,
                colon,
                waitFutureExpr);
    }

    public WaitFieldNodeModifier modify() {
        return new WaitFieldNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class WaitFieldNodeModifier {
        private final WaitFieldNode oldNode;
        private SimpleNameReferenceNode fieldName;
        private Token colon;
        private ExpressionNode waitFutureExpr;

        public WaitFieldNodeModifier(WaitFieldNode oldNode) {
            this.oldNode = oldNode;
            this.fieldName = oldNode.fieldName();
            this.colon = oldNode.colon();
            this.waitFutureExpr = oldNode.waitFutureExpr();
        }

        public WaitFieldNodeModifier withFieldName(
                SimpleNameReferenceNode fieldName) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            this.fieldName = fieldName;
            return this;
        }

        public WaitFieldNodeModifier withColon(
                Token colon) {
            Objects.requireNonNull(colon, "colon must not be null");
            this.colon = colon;
            return this;
        }

        public WaitFieldNodeModifier withWaitFutureExpr(
                ExpressionNode waitFutureExpr) {
            Objects.requireNonNull(waitFutureExpr, "waitFutureExpr must not be null");
            this.waitFutureExpr = waitFutureExpr;
            return this;
        }

        public WaitFieldNode apply() {
            return oldNode.modify(
                    fieldName,
                    colon,
                    waitFutureExpr);
        }
    }
}
