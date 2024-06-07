/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.10.0
 */
public class XMLStepIndexedExtendNode extends NonTerminalNode {

    public XMLStepIndexedExtendNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public Node expression() {
        return childInBucket(1);
    }

    public Token closeBracket() {
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
                "openBracket",
                "expression",
                "closeBracket"};
    }

    public XMLStepIndexedExtendNode modify(
            Token openBracket,
            Node expression,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                expression,
                closeBracket)) {
            return this;
        }

        return NodeFactory.createXMLStepIndexedExtendNode(
                openBracket,
                expression,
                closeBracket);
    }

    public XMLStepIndexedExtendNodeModifier modify() {
        return new XMLStepIndexedExtendNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.10.0
     */
    public static class XMLStepIndexedExtendNodeModifier {
        private final XMLStepIndexedExtendNode oldNode;
        private Token openBracket;
        private Node expression;
        private Token closeBracket;

        public XMLStepIndexedExtendNodeModifier(XMLStepIndexedExtendNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.expression = oldNode.expression();
            this.closeBracket = oldNode.closeBracket();
        }

        public XMLStepIndexedExtendNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public XMLStepIndexedExtendNodeModifier withExpression(
                Node expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public XMLStepIndexedExtendNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public XMLStepIndexedExtendNode apply() {
            return oldNode.modify(
                    openBracket,
                    expression,
                    closeBracket);
        }
    }
}
