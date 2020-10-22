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
public class TableTypeDescriptorNode extends TypeDescriptorNode {

    public TableTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token tableKeywordToken() {
        return childInBucket(0);
    }

    public Node rowTypeParameterNode() {
        return childInBucket(1);
    }

    public Optional<Node> keyConstraintNode() {
        return optionalChildInBucket(2);
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
                "tableKeywordToken",
                "rowTypeParameterNode",
                "keyConstraintNode"};
    }

    public TableTypeDescriptorNode modify(
            Token tableKeywordToken,
            Node rowTypeParameterNode,
            Node keyConstraintNode) {
        if (checkForReferenceEquality(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode)) {
            return this;
        }

        return NodeFactory.createTableTypeDescriptorNode(
                tableKeywordToken,
                rowTypeParameterNode,
                keyConstraintNode);
    }

    public TableTypeDescriptorNodeModifier modify() {
        return new TableTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TableTypeDescriptorNodeModifier {
        private final TableTypeDescriptorNode oldNode;
        private Token tableKeywordToken;
        private Node rowTypeParameterNode;
        private Node keyConstraintNode;

        public TableTypeDescriptorNodeModifier(TableTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.tableKeywordToken = oldNode.tableKeywordToken();
            this.rowTypeParameterNode = oldNode.rowTypeParameterNode();
            this.keyConstraintNode = oldNode.keyConstraintNode().orElse(null);
        }

        public TableTypeDescriptorNodeModifier withTableKeywordToken(
                Token tableKeywordToken) {
            Objects.requireNonNull(tableKeywordToken, "tableKeywordToken must not be null");
            this.tableKeywordToken = tableKeywordToken;
            return this;
        }

        public TableTypeDescriptorNodeModifier withRowTypeParameterNode(
                Node rowTypeParameterNode) {
            Objects.requireNonNull(rowTypeParameterNode, "rowTypeParameterNode must not be null");
            this.rowTypeParameterNode = rowTypeParameterNode;
            return this;
        }

        public TableTypeDescriptorNodeModifier withKeyConstraintNode(
                Node keyConstraintNode) {
            Objects.requireNonNull(keyConstraintNode, "keyConstraintNode must not be null");
            this.keyConstraintNode = keyConstraintNode;
            return this;
        }

        public TableTypeDescriptorNode apply() {
            return oldNode.modify(
                    tableKeywordToken,
                    rowTypeParameterNode,
                    keyConstraintNode);
        }
    }
}
