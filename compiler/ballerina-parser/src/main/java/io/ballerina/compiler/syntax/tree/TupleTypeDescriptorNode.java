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
public class TupleTypeDescriptorNode extends TypeDescriptorNode {

    public TupleTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracketToken() {
        return childInBucket(0);
    }

    public SeparatedNodeList<Node> memberTypeDesc() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token closeBracketToken() {
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
                "openBracketToken",
                "memberTypeDesc",
                "closeBracketToken"};
    }

    public TupleTypeDescriptorNode modify(
            Token openBracketToken,
            SeparatedNodeList<Node> memberTypeDesc,
            Token closeBracketToken) {
        if (checkForReferenceEquality(
                openBracketToken,
                memberTypeDesc.underlyingListNode(),
                closeBracketToken)) {
            return this;
        }

        return NodeFactory.createTupleTypeDescriptorNode(
                openBracketToken,
                memberTypeDesc,
                closeBracketToken);
    }

    public TupleTypeDescriptorNodeModifier modify() {
        return new TupleTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TupleTypeDescriptorNodeModifier {
        private final TupleTypeDescriptorNode oldNode;
        private Token openBracketToken;
        private SeparatedNodeList<Node> memberTypeDesc;
        private Token closeBracketToken;

        public TupleTypeDescriptorNodeModifier(TupleTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.openBracketToken = oldNode.openBracketToken();
            this.memberTypeDesc = oldNode.memberTypeDesc();
            this.closeBracketToken = oldNode.closeBracketToken();
        }

        public TupleTypeDescriptorNodeModifier withOpenBracketToken(
                Token openBracketToken) {
            Objects.requireNonNull(openBracketToken, "openBracketToken must not be null");
            this.openBracketToken = openBracketToken;
            return this;
        }

        public TupleTypeDescriptorNodeModifier withMemberTypeDesc(
                SeparatedNodeList<Node> memberTypeDesc) {
            Objects.requireNonNull(memberTypeDesc, "memberTypeDesc must not be null");
            this.memberTypeDesc = memberTypeDesc;
            return this;
        }

        public TupleTypeDescriptorNodeModifier withCloseBracketToken(
                Token closeBracketToken) {
            Objects.requireNonNull(closeBracketToken, "closeBracketToken must not be null");
            this.closeBracketToken = closeBracketToken;
            return this;
        }

        public TupleTypeDescriptorNode apply() {
            return oldNode.modify(
                    openBracketToken,
                    memberTypeDesc,
                    closeBracketToken);
        }
    }
}
