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
package syntax.tree;

import internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class TableArrayNode extends NonTerminalNode {

    public TableArrayNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public IdentifierToken identifier() {
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
                "identifier",
                "closeBracket"};
    }

    public TableArrayNode modify(
            Token openBracket,
            IdentifierToken identifier,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                identifier,
                closeBracket)) {
            return this;
        }

        return NodeFactory.createTableArrayNode(
                openBracket,
                identifier,
                closeBracket);
    }

    public TableArrayNodeModifier modify() {
        return new TableArrayNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TableArrayNodeModifier {
        private final TableArrayNode oldNode;
        private Token openBracket;
        private IdentifierToken identifier;
        private Token closeBracket;

        public TableArrayNodeModifier(TableArrayNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.identifier = oldNode.identifier();
            this.closeBracket = oldNode.closeBracket();
        }

        public TableArrayNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public TableArrayNodeModifier withIdentifier(
                IdentifierToken identifier) {
            Objects.requireNonNull(identifier, "identifier must not be null");
            this.identifier = identifier;
            return this;
        }

        public TableArrayNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public TableArrayNode apply() {
            return oldNode.modify(
                    openBracket,
                    identifier,
                    closeBracket);
        }
    }
}
