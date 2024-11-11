/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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
 * @since 2.0.0
 */
public class MappingBindingPatternNode extends BindingPatternNode {

    public MappingBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBrace() {
        return childInBucket(0);
    }

    public SeparatedNodeList<BindingPatternNode> fieldBindingPatterns() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token closeBrace() {
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
                "openBrace",
                "fieldBindingPatterns",
                "closeBrace"};
    }

    public MappingBindingPatternNode modify(
            Token openBrace,
            SeparatedNodeList<BindingPatternNode> fieldBindingPatterns,
            Token closeBrace) {
        if (checkForReferenceEquality(
                openBrace,
                fieldBindingPatterns.underlyingListNode(),
                closeBrace)) {
            return this;
        }

        return NodeFactory.createMappingBindingPatternNode(
                openBrace,
                fieldBindingPatterns,
                closeBrace);
    }

    public MappingBindingPatternNodeModifier modify() {
        return new MappingBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MappingBindingPatternNodeModifier {
        private final MappingBindingPatternNode oldNode;
        private Token openBrace;
        private SeparatedNodeList<BindingPatternNode> fieldBindingPatterns;
        private Token closeBrace;

        public MappingBindingPatternNodeModifier(MappingBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.openBrace = oldNode.openBrace();
            this.fieldBindingPatterns = oldNode.fieldBindingPatterns();
            this.closeBrace = oldNode.closeBrace();
        }

        public MappingBindingPatternNodeModifier withOpenBrace(
                Token openBrace) {
            Objects.requireNonNull(openBrace, "openBrace must not be null");
            this.openBrace = openBrace;
            return this;
        }

        public MappingBindingPatternNodeModifier withFieldBindingPatterns(
                SeparatedNodeList<BindingPatternNode> fieldBindingPatterns) {
            Objects.requireNonNull(fieldBindingPatterns, "fieldBindingPatterns must not be null");
            this.fieldBindingPatterns = fieldBindingPatterns;
            return this;
        }

        public MappingBindingPatternNodeModifier withCloseBrace(
                Token closeBrace) {
            Objects.requireNonNull(closeBrace, "closeBrace must not be null");
            this.closeBrace = closeBrace;
            return this;
        }

        public MappingBindingPatternNode apply() {
            return oldNode.modify(
                    openBrace,
                    fieldBindingPatterns,
                    closeBrace);
        }
    }
}
