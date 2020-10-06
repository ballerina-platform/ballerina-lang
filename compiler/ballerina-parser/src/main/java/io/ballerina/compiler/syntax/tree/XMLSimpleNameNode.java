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
public class XMLSimpleNameNode extends XMLNameNode {

    public XMLSimpleNameNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token name() {
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
                "name"};
    }

    public XMLSimpleNameNode modify(
            Token name) {
        if (checkForReferenceEquality(
                name)) {
            return this;
        }

        return NodeFactory.createXMLSimpleNameNode(
                name);
    }

    public XMLSimpleNameNodeModifier modify() {
        return new XMLSimpleNameNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLSimpleNameNodeModifier {
        private final XMLSimpleNameNode oldNode;
        private Token name;

        public XMLSimpleNameNodeModifier(XMLSimpleNameNode oldNode) {
            this.oldNode = oldNode;
            this.name = oldNode.name();
        }

        public XMLSimpleNameNodeModifier withName(
                Token name) {
            Objects.requireNonNull(name, "name must not be null");
            this.name = name;
            return this;
        }

        public XMLSimpleNameNode apply() {
            return oldNode.modify(
                    name);
        }
    }
}
