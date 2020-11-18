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
public class QualifiedNameReferenceNode extends NameReferenceNode {

    public QualifiedNameReferenceNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token modulePrefix() {
        return childInBucket(0);
    }

    public Node colon() {
        return childInBucket(1);
    }

    public IdentifierToken identifier() {
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
                "modulePrefix",
                "colon",
                "identifier"};
    }

    public QualifiedNameReferenceNode modify(
            Token modulePrefix,
            Node colon,
            IdentifierToken identifier) {
        if (checkForReferenceEquality(
                modulePrefix,
                colon,
                identifier)) {
            return this;
        }

        return NodeFactory.createQualifiedNameReferenceNode(
                modulePrefix,
                colon,
                identifier);
    }

    public QualifiedNameReferenceNodeModifier modify() {
        return new QualifiedNameReferenceNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class QualifiedNameReferenceNodeModifier {
        private final QualifiedNameReferenceNode oldNode;
        private Token modulePrefix;
        private Node colon;
        private IdentifierToken identifier;

        public QualifiedNameReferenceNodeModifier(QualifiedNameReferenceNode oldNode) {
            this.oldNode = oldNode;
            this.modulePrefix = oldNode.modulePrefix();
            this.colon = oldNode.colon();
            this.identifier = oldNode.identifier();
        }

        public QualifiedNameReferenceNodeModifier withModulePrefix(
                Token modulePrefix) {
            Objects.requireNonNull(modulePrefix, "modulePrefix must not be null");
            this.modulePrefix = modulePrefix;
            return this;
        }

        public QualifiedNameReferenceNodeModifier withColon(
                Node colon) {
            Objects.requireNonNull(colon, "colon must not be null");
            this.colon = colon;
            return this;
        }

        public QualifiedNameReferenceNodeModifier withIdentifier(
                IdentifierToken identifier) {
            Objects.requireNonNull(identifier, "identifier must not be null");
            this.identifier = identifier;
            return this;
        }

        public QualifiedNameReferenceNode apply() {
            return oldNode.modify(
                    modulePrefix,
                    colon,
                    identifier);
        }
    }
}
