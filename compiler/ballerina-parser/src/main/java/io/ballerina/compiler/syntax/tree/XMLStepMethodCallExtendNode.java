/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.11.0
 */
public class XMLStepMethodCallExtendNode extends NonTerminalNode {

    public XMLStepMethodCallExtendNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token dotToken() {
        return childInBucket(0);
    }

    public SimpleNameReferenceNode methodName() {
        return childInBucket(1);
    }

    public ParenthesizedArgList parenthesizedArgList() {
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
                "dotToken",
                "methodName",
                "parenthesizedArgList"};
    }

    public XMLStepMethodCallExtendNode modify(
            Token dotToken,
            SimpleNameReferenceNode methodName,
            ParenthesizedArgList parenthesizedArgList) {
        if (checkForReferenceEquality(
                dotToken,
                methodName,
                parenthesizedArgList)) {
            return this;
        }

        return NodeFactory.createXMLStepMethodCallExtendNode(
                dotToken,
                methodName,
                parenthesizedArgList);
    }

    public XMLStepMethodCallExtendNodeModifier modify() {
        return new XMLStepMethodCallExtendNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.10.0
     */
    public static class XMLStepMethodCallExtendNodeModifier {
        private final XMLStepMethodCallExtendNode oldNode;
        private Token dotToken;
        private SimpleNameReferenceNode methodName;
        private ParenthesizedArgList parenthesizedArgList;

        public XMLStepMethodCallExtendNodeModifier(XMLStepMethodCallExtendNode oldNode) {
            this.oldNode = oldNode;
            this.dotToken = oldNode.dotToken();
            this.methodName = oldNode.methodName();
            this.parenthesizedArgList = oldNode.parenthesizedArgList();
        }

        public XMLStepMethodCallExtendNodeModifier withDotToken(
                Token dotToken) {
            Objects.requireNonNull(dotToken, "dotToken must not be null");
            this.dotToken = dotToken;
            return this;
        }

        public XMLStepMethodCallExtendNodeModifier withMethodName(
                SimpleNameReferenceNode methodName) {
            Objects.requireNonNull(methodName, "methodName must not be null");
            this.methodName = methodName;
            return this;
        }

        public XMLStepMethodCallExtendNodeModifier withParenthesizedArgList(
                ParenthesizedArgList parenthesizedArgList) {
            Objects.requireNonNull(parenthesizedArgList, "parenthesizedArgList must not be null");
            this.parenthesizedArgList = parenthesizedArgList;
            return this;
        }

        public XMLStepMethodCallExtendNode apply() {
            return oldNode.modify(
                    dotToken,
                    methodName,
                    parenthesizedArgList);
        }
    }
}
