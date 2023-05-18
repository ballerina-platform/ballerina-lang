/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.3.0
 */
public class ReUnicodeScriptNode extends ReUnicodePropertyNode {

    public ReUnicodeScriptNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node scriptStart() {
        return childInBucket(0);
    }

    public Node reUnicodePropertyValue() {
        return childInBucket(1);
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
                "scriptStart",
                "reUnicodePropertyValue"};
    }

    public ReUnicodeScriptNode modify(
            Node scriptStart,
            Node reUnicodePropertyValue) {
        if (checkForReferenceEquality(
                scriptStart,
                reUnicodePropertyValue)) {
            return this;
        }

        return NodeFactory.createReUnicodeScriptNode(
                scriptStart,
                reUnicodePropertyValue);
    }

    public ReUnicodeScriptNodeModifier modify() {
        return new ReUnicodeScriptNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReUnicodeScriptNodeModifier {
        private final ReUnicodeScriptNode oldNode;
        private Node scriptStart;
        private Node reUnicodePropertyValue;

        public ReUnicodeScriptNodeModifier(ReUnicodeScriptNode oldNode) {
            this.oldNode = oldNode;
            this.scriptStart = oldNode.scriptStart();
            this.reUnicodePropertyValue = oldNode.reUnicodePropertyValue();
        }

        public ReUnicodeScriptNodeModifier withScriptStart(
                Node scriptStart) {
            Objects.requireNonNull(scriptStart, "scriptStart must not be null");
            this.scriptStart = scriptStart;
            return this;
        }

        public ReUnicodeScriptNodeModifier withReUnicodePropertyValue(
                Node reUnicodePropertyValue) {
            Objects.requireNonNull(reUnicodePropertyValue, "reUnicodePropertyValue must not be null");
            this.reUnicodePropertyValue = reUnicodePropertyValue;
            return this;
        }

        public ReUnicodeScriptNode apply() {
            return oldNode.modify(
                    scriptStart,
                    reUnicodePropertyValue);
        }
    }
}
