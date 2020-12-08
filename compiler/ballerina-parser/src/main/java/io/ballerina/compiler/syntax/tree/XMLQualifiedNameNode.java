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
public class XMLQualifiedNameNode extends XMLNameNode {

    public XMLQualifiedNameNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public XMLSimpleNameNode prefix() {
        return childInBucket(0);
    }

    public Token colon() {
        return childInBucket(1);
    }

    public XMLSimpleNameNode name() {
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
                "prefix",
                "colon",
                "name"};
    }

    public XMLQualifiedNameNode modify(
            XMLSimpleNameNode prefix,
            Token colon,
            XMLSimpleNameNode name) {
        if (checkForReferenceEquality(
                prefix,
                colon,
                name)) {
            return this;
        }

        return NodeFactory.createXMLQualifiedNameNode(
                prefix,
                colon,
                name);
    }

    public XMLQualifiedNameNodeModifier modify() {
        return new XMLQualifiedNameNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLQualifiedNameNodeModifier {
        private final XMLQualifiedNameNode oldNode;
        private XMLSimpleNameNode prefix;
        private Token colon;
        private XMLSimpleNameNode name;

        public XMLQualifiedNameNodeModifier(XMLQualifiedNameNode oldNode) {
            this.oldNode = oldNode;
            this.prefix = oldNode.prefix();
            this.colon = oldNode.colon();
            this.name = oldNode.name();
        }

        public XMLQualifiedNameNodeModifier withPrefix(
                XMLSimpleNameNode prefix) {
            Objects.requireNonNull(prefix, "prefix must not be null");
            this.prefix = prefix;
            return this;
        }

        public XMLQualifiedNameNodeModifier withColon(
                Token colon) {
            Objects.requireNonNull(colon, "colon must not be null");
            this.colon = colon;
            return this;
        }

        public XMLQualifiedNameNodeModifier withName(
                XMLSimpleNameNode name) {
            Objects.requireNonNull(name, "name must not be null");
            this.name = name;
            return this;
        }

        public XMLQualifiedNameNode apply() {
            return oldNode.modify(
                    prefix,
                    colon,
                    name);
        }
    }
}
