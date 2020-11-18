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
public class XMLProcessingInstruction extends XMLItemNode {

    public XMLProcessingInstruction(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token piStart() {
        return childInBucket(0);
    }

    public XMLNameNode target() {
        return childInBucket(1);
    }

    public NodeList<Node> data() {
        return new NodeList<>(childInBucket(2));
    }

    public Token piEnd() {
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
                "piStart",
                "target",
                "data",
                "piEnd"};
    }

    public XMLProcessingInstruction modify(
            Token piStart,
            XMLNameNode target,
            NodeList<Node> data,
            Token piEnd) {
        if (checkForReferenceEquality(
                piStart,
                target,
                data.underlyingListNode(),
                piEnd)) {
            return this;
        }

        return NodeFactory.createXMLProcessingInstruction(
                piStart,
                target,
                data,
                piEnd);
    }

    public XMLProcessingInstructionModifier modify() {
        return new XMLProcessingInstructionModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLProcessingInstructionModifier {
        private final XMLProcessingInstruction oldNode;
        private Token piStart;
        private XMLNameNode target;
        private NodeList<Node> data;
        private Token piEnd;

        public XMLProcessingInstructionModifier(XMLProcessingInstruction oldNode) {
            this.oldNode = oldNode;
            this.piStart = oldNode.piStart();
            this.target = oldNode.target();
            this.data = oldNode.data();
            this.piEnd = oldNode.piEnd();
        }

        public XMLProcessingInstructionModifier withPiStart(
                Token piStart) {
            Objects.requireNonNull(piStart, "piStart must not be null");
            this.piStart = piStart;
            return this;
        }

        public XMLProcessingInstructionModifier withTarget(
                XMLNameNode target) {
            Objects.requireNonNull(target, "target must not be null");
            this.target = target;
            return this;
        }

        public XMLProcessingInstructionModifier withData(
                NodeList<Node> data) {
            Objects.requireNonNull(data, "data must not be null");
            this.data = data;
            return this;
        }

        public XMLProcessingInstructionModifier withPiEnd(
                Token piEnd) {
            Objects.requireNonNull(piEnd, "piEnd must not be null");
            this.piEnd = piEnd;
            return this;
        }

        public XMLProcessingInstruction apply() {
            return oldNode.modify(
                    piStart,
                    target,
                    data,
                    piEnd);
        }
    }
}
