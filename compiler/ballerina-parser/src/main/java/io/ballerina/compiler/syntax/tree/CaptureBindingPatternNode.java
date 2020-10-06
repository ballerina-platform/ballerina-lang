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
public class CaptureBindingPatternNode extends BindingPatternNode {

    public CaptureBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token variableName() {
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
                "variableName"};
    }

    public CaptureBindingPatternNode modify(
            Token variableName) {
        if (checkForReferenceEquality(
                variableName)) {
            return this;
        }

        return NodeFactory.createCaptureBindingPatternNode(
                variableName);
    }

    public CaptureBindingPatternNodeModifier modify() {
        return new CaptureBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class CaptureBindingPatternNodeModifier {
        private final CaptureBindingPatternNode oldNode;
        private Token variableName;

        public CaptureBindingPatternNodeModifier(CaptureBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.variableName = oldNode.variableName();
        }

        public CaptureBindingPatternNodeModifier withVariableName(
                Token variableName) {
            Objects.requireNonNull(variableName, "variableName must not be null");
            this.variableName = variableName;
            return this;
        }

        public CaptureBindingPatternNode apply() {
            return oldNode.modify(
                    variableName);
        }
    }
}
