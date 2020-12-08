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
public class SingletonTypeDescriptorNode extends TypeDescriptorNode {

    public SingletonTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode simpleContExprNode() {
        return childInBucket(0);
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
                "simpleContExprNode"};
    }

    public SingletonTypeDescriptorNode modify(
            ExpressionNode simpleContExprNode) {
        if (checkForReferenceEquality(
                simpleContExprNode)) {
            return this;
        }

        return NodeFactory.createSingletonTypeDescriptorNode(
                simpleContExprNode);
    }

    public SingletonTypeDescriptorNodeModifier modify() {
        return new SingletonTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class SingletonTypeDescriptorNodeModifier {
        private final SingletonTypeDescriptorNode oldNode;
        private ExpressionNode simpleContExprNode;

        public SingletonTypeDescriptorNodeModifier(SingletonTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.simpleContExprNode = oldNode.simpleContExprNode();
        }

        public SingletonTypeDescriptorNodeModifier withSimpleContExprNode(
                ExpressionNode simpleContExprNode) {
            Objects.requireNonNull(simpleContExprNode, "simpleContExprNode must not be null");
            this.simpleContExprNode = simpleContExprNode;
            return this;
        }

        public SingletonTypeDescriptorNode apply() {
            return oldNode.modify(
                    simpleContExprNode);
        }
    }
}
