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
public class ForEachStatementNode extends StatementNode {

    public ForEachStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token forEachKeyword() {
        return childInBucket(0);
    }

    public Node typeDescriptor() {
        return childInBucket(1);
    }

    public Token variableName() {
        return childInBucket(2);
    }

    public Token inKeyword() {
        return childInBucket(3);
    }

    public Node actionOrExpressionNode() {
        return childInBucket(4);
    }

    public StatementNode blockStatement() {
        return childInBucket(5);
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
                "forEachKeyword",
                "typeDescriptor",
                "variableName",
                "inKeyword",
                "actionOrExpressionNode",
                "blockStatement"};
    }

    public ForEachStatementNode modify(
            Token forEachKeyword,
            Node typeDescriptor,
            Token variableName,
            Token inKeyword,
            Node actionOrExpressionNode,
            StatementNode blockStatement) {
        if (checkForReferenceEquality(
                forEachKeyword,
                typeDescriptor,
                variableName,
                inKeyword,
                actionOrExpressionNode,
                blockStatement)) {
            return this;
        }

        return NodeFactory.createForEachStatementNode(
                forEachKeyword,
                typeDescriptor,
                variableName,
                inKeyword,
                actionOrExpressionNode,
                blockStatement);
    }

    public ForEachStatementNodeModifier modify() {
        return new ForEachStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ForEachStatementNodeModifier {
        private final ForEachStatementNode oldNode;
        private Token forEachKeyword;
        private Node typeDescriptor;
        private Token variableName;
        private Token inKeyword;
        private Node ActionOrExpressionNode;
        private StatementNode blockStatement;

        public ForEachStatementNodeModifier(ForEachStatementNode oldNode) {
            this.oldNode = oldNode;
            this.forEachKeyword = oldNode.forEachKeyword();
            this.typeDescriptor = oldNode.typeDescriptor();
            this.variableName = oldNode.variableName();
            this.inKeyword = oldNode.inKeyword();
            this.ActionOrExpressionNode = oldNode.ActionOrExpressionNode();
            this.blockStatement = oldNode.blockStatement();
        }

        public ForEachStatementNodeModifier withForEachKeyword(Token forEachKeyword) {
            Objects.requireNonNull(forEachKeyword, "forEachKeyword must not be null");
            this.forEachKeyword = forEachKeyword;
            return this;
        }

        public ForEachStatementNodeModifier withTypeDescriptor(Node typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public ForEachStatementNodeModifier withVariableName(Token variableName) {
            Objects.requireNonNull(variableName, "variableName must not be null");
            this.variableName = variableName;
            return this;
        }

        public ForEachStatementNodeModifier withInKeyword(Token inKeyword) {
            Objects.requireNonNull(inKeyword, "inKeyword must not be null");
            this.inKeyword = inKeyword;
            return this;
        }

        public ForEachStatementNodeModifier withActionOrExpressionNode(Node ActionOrExpressionNode) {
            Objects.requireNonNull(ActionOrExpressionNode, "ActionOrExpressionNode must not be null");
            this.ActionOrExpressionNode = ActionOrExpressionNode;
            return this;
        }

        public ForEachStatementNodeModifier withBlockStatement(StatementNode blockStatement) {
            Objects.requireNonNull(blockStatement, "blockStatement must not be null");
            this.blockStatement = blockStatement;
            return this;
        }

        public ForEachStatementNode apply() {
            return oldNode.modify(
                    forEachKeyword,
                    typeDescriptor,
                    variableName,
                    inKeyword,
                    ActionOrExpressionNode,
                    blockStatement);
        }
    }
}
