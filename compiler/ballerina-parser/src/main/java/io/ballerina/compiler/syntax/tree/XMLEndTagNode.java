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
public class XMLEndTagNode extends XMLElementTagNode {

    public XMLEndTagNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ltToken() {
        return childInBucket(0);
    }

    public Token slashToken() {
        return childInBucket(1);
    }

    public XMLNameNode name() {
        return childInBucket(2);
    }

    public Token getToken() {
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
                "ltToken",
                "slashToken",
                "name",
                "getToken"};
    }

    public XMLEndTagNode modify(
            Token ltToken,
            Token slashToken,
            XMLNameNode name,
            Token getToken) {
        if (checkForReferenceEquality(
                ltToken,
                slashToken,
                name,
                getToken)) {
            return this;
        }

        return NodeFactory.createXMLEndTagNode(
                ltToken,
                slashToken,
                name,
                getToken);
    }

    public XMLEndTagNodeModifier modify() {
        return new XMLEndTagNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLEndTagNodeModifier {
        private final XMLEndTagNode oldNode;
        private Token ltToken;
        private Token slashToken;
        private XMLNameNode name;
        private Token getToken;

        public XMLEndTagNodeModifier(XMLEndTagNode oldNode) {
            this.oldNode = oldNode;
            this.ltToken = oldNode.ltToken();
            this.slashToken = oldNode.slashToken();
            this.name = oldNode.name();
            this.getToken = oldNode.getToken();
        }

        public XMLEndTagNodeModifier withLtToken(
                Token ltToken) {
            Objects.requireNonNull(ltToken, "ltToken must not be null");
            this.ltToken = ltToken;
            return this;
        }

        public XMLEndTagNodeModifier withSlashToken(
                Token slashToken) {
            Objects.requireNonNull(slashToken, "slashToken must not be null");
            this.slashToken = slashToken;
            return this;
        }

        public XMLEndTagNodeModifier withName(
                XMLNameNode name) {
            Objects.requireNonNull(name, "name must not be null");
            this.name = name;
            return this;
        }

        public XMLEndTagNodeModifier withGetToken(
                Token getToken) {
            Objects.requireNonNull(getToken, "getToken must not be null");
            this.getToken = getToken;
            return this;
        }

        public XMLEndTagNode apply() {
            return oldNode.modify(
                    ltToken,
                    slashToken,
                    name,
                    getToken);
        }
    }
}
