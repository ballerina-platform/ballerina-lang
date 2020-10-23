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
public class ArrayTypeDescriptorNode extends TypeDescriptorNode {

    public ArrayTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public TypeDescriptorNode memberTypeDesc() {
        return childInBucket(0);
    }

    public Token openBracket() {
        return childInBucket(1);
    }

    public Optional<Node> arrayLength() {
        return optionalChildInBucket(2);
    }

    public Token closeBracket() {
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
                "memberTypeDesc",
                "openBracket",
                "arrayLength",
                "closeBracket"};
    }

    public ArrayTypeDescriptorNode modify(
            TypeDescriptorNode memberTypeDesc,
            Token openBracket,
            Node arrayLength,
            Token closeBracket) {
        if (checkForReferenceEquality(
                memberTypeDesc,
                openBracket,
                arrayLength,
                closeBracket)) {
            return this;
        }

        return NodeFactory.createArrayTypeDescriptorNode(
                memberTypeDesc,
                openBracket,
                arrayLength,
                closeBracket);
    }

    public ArrayTypeDescriptorNodeModifier modify() {
        return new ArrayTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ArrayTypeDescriptorNodeModifier {
        private final ArrayTypeDescriptorNode oldNode;
        private TypeDescriptorNode memberTypeDesc;
        private Token openBracket;
        private Node arrayLength;
        private Token closeBracket;

        public ArrayTypeDescriptorNodeModifier(ArrayTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.memberTypeDesc = oldNode.memberTypeDesc();
            this.openBracket = oldNode.openBracket();
            this.arrayLength = oldNode.arrayLength().orElse(null);
            this.closeBracket = oldNode.closeBracket();
        }

        public ArrayTypeDescriptorNodeModifier withMemberTypeDesc(
                TypeDescriptorNode memberTypeDesc) {
            Objects.requireNonNull(memberTypeDesc, "memberTypeDesc must not be null");
            this.memberTypeDesc = memberTypeDesc;
            return this;
        }

        public ArrayTypeDescriptorNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public ArrayTypeDescriptorNodeModifier withArrayLength(
                Node arrayLength) {
            Objects.requireNonNull(arrayLength, "arrayLength must not be null");
            this.arrayLength = arrayLength;
            return this;
        }

        public ArrayTypeDescriptorNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public ArrayTypeDescriptorNode apply() {
            return oldNode.modify(
                    memberTypeDesc,
                    openBracket,
                    arrayLength,
                    closeBracket);
        }
    }
}
