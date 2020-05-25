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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class FieldBindingPatternNode extends NonTerminalNode {

    public FieldBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SimpleNameReferenceNode variableName() {
        return childInBucket(0);
    }

    public Optional<Token> colon() {
        return optionalChildInBucket(1);
    }

    public Optional<BindingPatternNode> bindingPattern() {
        return optionalChildInBucket(2);
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
                "variableName",
                "colon",
                "bindingPattern"};
    }

    public FieldBindingPatternNode modify(
            SimpleNameReferenceNode variableName,
            Token colon,
            BindingPatternNode bindingPattern) {
        if (checkForReferenceEquality(
                variableName,
                colon,
                bindingPattern)) {
            return this;
        }

        return NodeFactory.createFieldBindingPatternNode(
                variableName,
                colon,
                bindingPattern);
    }

    public FieldBindingPatternNodeModifier modify() {
        return new FieldBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FieldBindingPatternNodeModifier {
        private final FieldBindingPatternNode oldNode;
        private SimpleNameReferenceNode variableName;
        private Token colon;
        private BindingPatternNode bindingPattern;

        public FieldBindingPatternNodeModifier(FieldBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.variableName = oldNode.variableName();
            this.colon = oldNode.colon().orElse(null);
            this.bindingPattern = oldNode.bindingPattern().orElse(null);
        }

        public FieldBindingPatternNodeModifier withVariableName(
                SimpleNameReferenceNode variableName) {
            Objects.requireNonNull(variableName, "variableName must not be null");
            this.variableName = variableName;
            return this;
        }

        public FieldBindingPatternNodeModifier withColon(
                Token colon) {
            Objects.requireNonNull(colon, "colon must not be null");
            this.colon = colon;
            return this;
        }

        public FieldBindingPatternNodeModifier withBindingPattern(
                BindingPatternNode bindingPattern) {
            Objects.requireNonNull(bindingPattern, "bindingPattern must not be null");
            this.bindingPattern = bindingPattern;
            return this;
        }

        public FieldBindingPatternNode apply() {
            return oldNode.modify(
                    variableName,
                    colon,
                    bindingPattern);
        }
    }
}
