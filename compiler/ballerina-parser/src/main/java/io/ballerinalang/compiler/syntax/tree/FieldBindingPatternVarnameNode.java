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
public class FieldBindingPatternVarnameNode extends FieldBindingPatternNode {

    public FieldBindingPatternVarnameNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SimpleNameReferenceNode variableName() {
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

    public FieldBindingPatternVarnameNode modify(
            SimpleNameReferenceNode variableName) {
        if (checkForReferenceEquality(
                variableName)) {
            return this;
        }

        return NodeFactory.createFieldBindingPatternVarnameNode(
                variableName);
    }

    public FieldBindingPatternVarnameNodeModifier modify() {
        return new FieldBindingPatternVarnameNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FieldBindingPatternVarnameNodeModifier {
        private final FieldBindingPatternVarnameNode oldNode;
        private SimpleNameReferenceNode variableName;

        public FieldBindingPatternVarnameNodeModifier(FieldBindingPatternVarnameNode oldNode) {
            this.oldNode = oldNode;
            this.variableName = oldNode.variableName();
        }

        public FieldBindingPatternVarnameNodeModifier withVariableName(
                SimpleNameReferenceNode variableName) {
            Objects.requireNonNull(variableName, "variableName must not be null");
            this.variableName = variableName;
            return this;
        }

        public FieldBindingPatternVarnameNode apply() {
            return oldNode.modify(
                    variableName);
        }
    }
}
