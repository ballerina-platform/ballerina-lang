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
public class XMLAtomicNamePatternNode extends NonTerminalNode {

    public XMLAtomicNamePatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token prefix() {
        return childInBucket(0);
    }

    public Token colon() {
        return childInBucket(1);
    }

    public Token name() {
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

    public XMLAtomicNamePatternNode modify(
            Token prefix,
            Token colon,
            Token name) {
        if (checkForReferenceEquality(
                prefix,
                colon,
                name)) {
            return this;
        }

        return NodeFactory.createXMLAtomicNamePatternNode(
                prefix,
                colon,
                name);
    }

    public XMLAtomicNamePatternNodeModifier modify() {
        return new XMLAtomicNamePatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLAtomicNamePatternNodeModifier {
        private final XMLAtomicNamePatternNode oldNode;
        private Token prefix;
        private Token colon;
        private Token name;

        public XMLAtomicNamePatternNodeModifier(XMLAtomicNamePatternNode oldNode) {
            this.oldNode = oldNode;
            this.prefix = oldNode.prefix();
            this.colon = oldNode.colon();
            this.name = oldNode.name();
        }

        public XMLAtomicNamePatternNodeModifier withPrefix(
                Token prefix) {
            Objects.requireNonNull(prefix, "prefix must not be null");
            this.prefix = prefix;
            return this;
        }

        public XMLAtomicNamePatternNodeModifier withColon(
                Token colon) {
            Objects.requireNonNull(colon, "colon must not be null");
            this.colon = colon;
            return this;
        }

        public XMLAtomicNamePatternNodeModifier withName(
                Token name) {
            Objects.requireNonNull(name, "name must not be null");
            this.name = name;
            return this;
        }

        public XMLAtomicNamePatternNode apply() {
            return oldNode.modify(
                    prefix,
                    colon,
                    name);
        }
    }
}
