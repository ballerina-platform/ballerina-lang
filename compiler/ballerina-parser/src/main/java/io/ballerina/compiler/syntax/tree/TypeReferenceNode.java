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
public class TypeReferenceNode extends TypeDescriptorNode {

    public TypeReferenceNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token asteriskToken() {
        return childInBucket(0);
    }

    public Node typeName() {
        return childInBucket(1);
    }

    public Token semicolonToken() {
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
                "asteriskToken",
                "typeName",
                "semicolonToken"};
    }

    public TypeReferenceNode modify(
            Token asteriskToken,
            Node typeName,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                asteriskToken,
                typeName,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createTypeReferenceNode(
                asteriskToken,
                typeName,
                semicolonToken);
    }

    public TypeReferenceNodeModifier modify() {
        return new TypeReferenceNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TypeReferenceNodeModifier {
        private final TypeReferenceNode oldNode;
        private Token asteriskToken;
        private Node typeName;
        private Token semicolonToken;

        public TypeReferenceNodeModifier(TypeReferenceNode oldNode) {
            this.oldNode = oldNode;
            this.asteriskToken = oldNode.asteriskToken();
            this.typeName = oldNode.typeName();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public TypeReferenceNodeModifier withAsteriskToken(
                Token asteriskToken) {
            Objects.requireNonNull(asteriskToken, "asteriskToken must not be null");
            this.asteriskToken = asteriskToken;
            return this;
        }

        public TypeReferenceNodeModifier withTypeName(
                Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public TypeReferenceNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public TypeReferenceNode apply() {
            return oldNode.modify(
                    asteriskToken,
                    typeName,
                    semicolonToken);
        }
    }
}
