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
public class NamedArgBindingPatternNode extends BindingPatternNode {

    public NamedArgBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public IdentifierToken argName() {
        return childInBucket(0);
    }

    public Token equalsToken() {
        return childInBucket(1);
    }

    public BindingPatternNode bindingPattern() {
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
                "argName",
                "equalsToken",
                "bindingPattern"};
    }

    public NamedArgBindingPatternNode modify(
            IdentifierToken argName,
            Token equalsToken,
            BindingPatternNode bindingPattern) {
        if (checkForReferenceEquality(
                argName,
                equalsToken,
                bindingPattern)) {
            return this;
        }

        return NodeFactory.createNamedArgBindingPatternNode(
                argName,
                equalsToken,
                bindingPattern);
    }

    public NamedArgBindingPatternNodeModifier modify() {
        return new NamedArgBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class NamedArgBindingPatternNodeModifier {
        private final NamedArgBindingPatternNode oldNode;
        private IdentifierToken argName;
        private Token equalsToken;
        private BindingPatternNode bindingPattern;

        public NamedArgBindingPatternNodeModifier(NamedArgBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.argName = oldNode.argName();
            this.equalsToken = oldNode.equalsToken();
            this.bindingPattern = oldNode.bindingPattern();
        }

        public NamedArgBindingPatternNodeModifier withArgName(
                IdentifierToken argName) {
            Objects.requireNonNull(argName, "argName must not be null");
            this.argName = argName;
            return this;
        }

        public NamedArgBindingPatternNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public NamedArgBindingPatternNodeModifier withBindingPattern(
                BindingPatternNode bindingPattern) {
            Objects.requireNonNull(bindingPattern, "bindingPattern must not be null");
            this.bindingPattern = bindingPattern;
            return this;
        }

        public NamedArgBindingPatternNode apply() {
            return oldNode.modify(
                    argName,
                    equalsToken,
                    bindingPattern);
        }
    }
}
