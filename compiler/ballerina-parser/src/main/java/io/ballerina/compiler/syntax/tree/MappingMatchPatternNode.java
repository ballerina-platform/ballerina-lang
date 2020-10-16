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
public class MappingMatchPatternNode extends NonTerminalNode {

    public MappingMatchPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBraceToken() {
        return childInBucket(0);
    }

    public SeparatedNodeList<FieldMatchPatternNode> fieldMatchPatterns() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Optional<RestMatchPatternNode> restMatchPattern() {
        return optionalChildInBucket(2);
    }

    public Token closeBraceToken() {
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
                "openBraceToken",
                "fieldMatchPatterns",
                "restMatchPattern",
                "closeBraceToken"};
    }

    public MappingMatchPatternNode modify(
            Token openBraceToken,
            SeparatedNodeList<FieldMatchPatternNode> fieldMatchPatterns,
            RestMatchPatternNode restMatchPattern,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                openBraceToken,
                fieldMatchPatterns.underlyingListNode(),
                restMatchPattern,
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createMappingMatchPatternNode(
                openBraceToken,
                fieldMatchPatterns,
                restMatchPattern,
                closeBraceToken);
    }

    public MappingMatchPatternNodeModifier modify() {
        return new MappingMatchPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MappingMatchPatternNodeModifier {
        private final MappingMatchPatternNode oldNode;
        private Token openBraceToken;
        private SeparatedNodeList<FieldMatchPatternNode> fieldMatchPatterns;
        private RestMatchPatternNode restMatchPattern;
        private Token closeBraceToken;

        public MappingMatchPatternNodeModifier(MappingMatchPatternNode oldNode) {
            this.oldNode = oldNode;
            this.openBraceToken = oldNode.openBraceToken();
            this.fieldMatchPatterns = oldNode.fieldMatchPatterns();
            this.restMatchPattern = oldNode.restMatchPattern().orElse(null);
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public MappingMatchPatternNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public MappingMatchPatternNodeModifier withFieldMatchPatterns(
                SeparatedNodeList<FieldMatchPatternNode> fieldMatchPatterns) {
            Objects.requireNonNull(fieldMatchPatterns, "fieldMatchPatterns must not be null");
            this.fieldMatchPatterns = fieldMatchPatterns;
            return this;
        }

        public MappingMatchPatternNodeModifier withRestMatchPattern(
                RestMatchPatternNode restMatchPattern) {
            Objects.requireNonNull(restMatchPattern, "restMatchPattern must not be null");
            this.restMatchPattern = restMatchPattern;
            return this;
        }

        public MappingMatchPatternNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public MappingMatchPatternNode apply() {
            return oldNode.modify(
                    openBraceToken,
                    fieldMatchPatterns,
                    restMatchPattern,
                    closeBraceToken);
        }
    }
}
